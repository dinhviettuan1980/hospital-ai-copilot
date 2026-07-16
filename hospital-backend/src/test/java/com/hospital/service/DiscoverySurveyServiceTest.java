package com.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hospital.dto.DiscoveryAnswerRequest;
import com.hospital.dto.DiscoveryAnswerResponse;
import com.hospital.dto.DiscoverySectionProgressResponse;
import com.hospital.entity.DiscoveryAnswer;
import com.hospital.entity.DiscoveryAnswerType;
import com.hospital.entity.DiscoveryProject;
import com.hospital.entity.DiscoveryQuestion;
import com.hospital.entity.DiscoveryRiskLevel;
import com.hospital.entity.DiscoverySection;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.DiscoveryAnswerMapper;
import com.hospital.mapper.DiscoveryAttachmentMapper;
import com.hospital.mapper.DiscoveryProjectMapper;
import com.hospital.mapper.DiscoverySectionMapper;
import com.hospital.mapper.DiscoveryQuestionMapper;
import com.hospital.repository.DiscoveryAnswerRepository;
import com.hospital.repository.DiscoveryAttachmentRepository;
import com.hospital.repository.DiscoveryProjectRepository;
import com.hospital.repository.DiscoveryQuestionRepository;
import com.hospital.repository.DiscoverySectionRepository;

class DiscoverySurveyServiceTest {

    private DiscoverySectionRepository discoverySectionRepository;
    private DiscoveryQuestionRepository discoveryQuestionRepository;
    private DiscoveryAnswerRepository discoveryAnswerRepository;
    private DiscoveryAttachmentRepository discoveryAttachmentRepository;
    private DiscoveryProjectRepository discoveryProjectRepository;
    private DiscoverySurveyService discoverySurveyService;

    private DiscoveryProject project;
    private DiscoverySection section;
    private DiscoveryQuestion question;

    @BeforeEach
    void setUp() {
        discoverySectionRepository = mock(DiscoverySectionRepository.class);
        discoveryQuestionRepository = mock(DiscoveryQuestionRepository.class);
        discoveryAnswerRepository = mock(DiscoveryAnswerRepository.class);
        discoveryAttachmentRepository = mock(DiscoveryAttachmentRepository.class);
        discoveryProjectRepository = mock(DiscoveryProjectRepository.class);

        DiscoveryProjectService discoveryProjectService = new DiscoveryProjectService(discoveryProjectRepository,
                discoveryAnswerRepository, mock(DiscoveryAttachmentService.class), new DiscoveryProjectMapper(),
                new DiscoveryProgressCalculator(discoveryQuestionRepository, discoveryAnswerRepository));

        discoverySurveyService = new DiscoverySurveyService(discoverySectionRepository, discoveryQuestionRepository,
                discoveryAnswerRepository, discoveryAttachmentRepository, new DiscoverySectionMapper(),
                new DiscoveryQuestionMapper(new com.fasterxml.jackson.databind.ObjectMapper()),
                new DiscoveryAnswerMapper(), new DiscoveryAttachmentMapper(), discoveryProjectService);

        project = new DiscoveryProject();
        project.id = UUID.randomUUID();

        section = new DiscoverySection();
        section.id = UUID.randomUUID();
        section.code = "OVERVIEW";
        section.name = "Hospital Overview";
        section.displayOrder = 1;

        question = new DiscoveryQuestion();
        question.id = UUID.randomUUID();
        question.section = section;
        question.code = "OVERVIEW-01";
        question.title = "What is the hospital name?";
        question.answerType = DiscoveryAnswerType.TEXT;
        question.displayOrder = 1;

        when(discoveryProjectRepository.findByIdOptional(project.id)).thenReturn(Optional.of(project));
    }

    @Test
    void saveAnswerCreatesNewAnswerWhenNoneExists() {
        when(discoveryQuestionRepository.findByIdOptional(question.id)).thenReturn(Optional.of(question));
        when(discoveryAnswerRepository.findByProjectAndQuestion(project.id, question.id)).thenReturn(Optional.empty());

        DiscoveryAnswerRequest request = new DiscoveryAnswerRequest("Hanoi Heart Hospital", "Confirmed on-site",
                DiscoveryRiskLevel.LOW);
        DiscoveryAnswerResponse response = discoverySurveyService.saveAnswer(project.id, question.id, request);

        assertThat(response.answerValue()).isEqualTo("Hanoi Heart Hospital");
        assertThat(response.riskLevel()).isEqualTo(DiscoveryRiskLevel.LOW);
        verify(discoveryAnswerRepository).persist(any(DiscoveryAnswer.class));
    }

    @Test
    void saveAnswerUpdatesExistingAnswerInPlaceWithoutPersistingAgain() {
        when(discoveryQuestionRepository.findByIdOptional(question.id)).thenReturn(Optional.of(question));
        DiscoveryAnswer existing = new DiscoveryAnswer();
        existing.id = UUID.randomUUID();
        existing.project = project;
        existing.question = question;
        existing.answerValue = "Old value";
        when(discoveryAnswerRepository.findByProjectAndQuestion(project.id, question.id))
                .thenReturn(Optional.of(existing));

        DiscoveryAnswerRequest request = new DiscoveryAnswerRequest("New value", null, null);
        DiscoveryAnswerResponse response = discoverySurveyService.saveAnswer(project.id, question.id, request);

        assertThat(response.answerValue()).isEqualTo("New value");
        assertThat(existing.answerValue).isEqualTo("New value");
        verify(discoveryAnswerRepository, never()).persist(any(DiscoveryAnswer.class));
    }

    @Test
    void saveAnswerThrowsWhenQuestionDoesNotExist() {
        UUID unknownQuestionId = UUID.randomUUID();
        when(discoveryQuestionRepository.findByIdOptional(unknownQuestionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> discoverySurveyService.saveAnswer(project.id, unknownQuestionId,
                new DiscoveryAnswerRequest("x", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listSectionsWithProgressComputesAnsweredCountPerSection() {
        when(discoverySectionRepository.listAllOrdered()).thenReturn(List.of(section));
        when(discoveryQuestionRepository.listBySection(section.id)).thenReturn(List.of(question));
        DiscoveryAnswer answer = new DiscoveryAnswer();
        answer.question = question;
        when(discoveryAnswerRepository.listByProject(project.id)).thenReturn(List.of(answer));

        List<DiscoverySectionProgressResponse> result = discoverySurveyService.listSectionsWithProgress(project.id);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).totalQuestions()).isEqualTo(1);
        assertThat(result.get(0).answeredQuestions()).isEqualTo(1);
        assertThat(result.get(0).percent()).isEqualTo(100.0);
    }

    @Test
    void listQuestionsIncludesCurrentAnswerAndAttachments() {
        when(discoveryQuestionRepository.listBySection(section.id)).thenReturn(List.of(question));
        DiscoveryAnswer answer = new DiscoveryAnswer();
        answer.question = question;
        answer.answerValue = "Answered";
        when(discoveryAnswerRepository.listByProject(project.id)).thenReturn(List.of(answer));
        when(discoveryAttachmentRepository.listByProject(project.id)).thenReturn(List.of());

        var result = discoverySurveyService.listQuestions(project.id, section.id);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).answer().answerValue()).isEqualTo("Answered");
    }
}
