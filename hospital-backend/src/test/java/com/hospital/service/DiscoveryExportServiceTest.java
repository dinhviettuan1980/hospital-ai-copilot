package com.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.hospital.dto.DiscoverySurveyExport;
import com.hospital.entity.DiscoveryAnswer;
import com.hospital.entity.DiscoveryAnswerType;
import com.hospital.entity.DiscoveryProject;
import com.hospital.entity.DiscoveryProjectStatus;
import com.hospital.entity.DiscoveryQuestion;
import com.hospital.entity.DiscoverySection;
import com.hospital.mapper.DiscoveryAnswerMapper;
import com.hospital.mapper.DiscoveryAttachmentMapper;
import com.hospital.mapper.DiscoveryProjectMapper;
import com.hospital.mapper.DiscoveryQuestionMapper;
import com.hospital.repository.DiscoveryAnswerRepository;
import com.hospital.repository.DiscoveryAttachmentRepository;
import com.hospital.repository.DiscoveryProjectRepository;
import com.hospital.repository.DiscoveryQuestionRepository;
import com.hospital.repository.DiscoverySectionRepository;

class DiscoveryExportServiceTest {

    private DiscoveryProjectRepository discoveryProjectRepository;
    private DiscoverySectionRepository discoverySectionRepository;
    private DiscoveryQuestionRepository discoveryQuestionRepository;
    private DiscoveryAnswerRepository discoveryAnswerRepository;
    private DiscoveryAttachmentRepository discoveryAttachmentRepository;
    private DiscoveryExportService discoveryExportService;

    private DiscoveryProject project;
    private DiscoverySection section;
    private DiscoveryQuestion question;

    @BeforeEach
    void setUp() {
        discoveryProjectRepository = mock(DiscoveryProjectRepository.class);
        discoverySectionRepository = mock(DiscoverySectionRepository.class);
        discoveryQuestionRepository = mock(DiscoveryQuestionRepository.class);
        discoveryAnswerRepository = mock(DiscoveryAnswerRepository.class);
        discoveryAttachmentRepository = mock(DiscoveryAttachmentRepository.class);

        DiscoveryProgressCalculator progressCalculator = new DiscoveryProgressCalculator(discoveryQuestionRepository,
                discoveryAnswerRepository);
        DiscoveryProjectService discoveryProjectService = new DiscoveryProjectService(discoveryProjectRepository,
                discoveryAnswerRepository, mock(DiscoveryAttachmentService.class), new DiscoveryProjectMapper(),
                progressCalculator);

        discoveryExportService = new DiscoveryExportService(discoveryProjectService, discoverySectionRepository,
                discoveryQuestionRepository, discoveryAnswerRepository, discoveryAttachmentRepository,
                new DiscoveryProjectMapper(), new DiscoveryQuestionMapper(new ObjectMapper()),
                new DiscoveryAnswerMapper(), new DiscoveryAttachmentMapper(), progressCalculator);

        project = new DiscoveryProject();
        project.id = UUID.randomUUID();
        project.projectName = "Discovery Project";
        project.hospitalName = "Test Hospital";
        project.status = DiscoveryProjectStatus.IN_PROGRESS;

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
        when(discoverySectionRepository.listAllOrdered()).thenReturn(List.of(section));
        when(discoveryQuestionRepository.listBySection(section.id)).thenReturn(List.of(question));
        when(discoveryAttachmentRepository.listByProject(project.id)).thenReturn(List.of());
    }

    @Test
    void exportIncludesProjectSectionsQuestionsAndAnswers() {
        DiscoveryAnswer answer = new DiscoveryAnswer();
        answer.question = question;
        answer.answerValue = "Hanoi Heart Hospital";
        when(discoveryAnswerRepository.listByProject(project.id)).thenReturn(List.of(answer));
        when(discoveryQuestionRepository.count()).thenReturn(1L);
        when(discoveryAnswerRepository.countByProject(project.id)).thenReturn(1L);

        DiscoverySurveyExport export = discoveryExportService.export(project.id);

        assertThat(export.exportVersion()).isEqualTo(DiscoverySurveyExport.CURRENT_VERSION);
        assertThat(export.project().projectName()).isEqualTo("Discovery Project");
        assertThat(export.progress().percent()).isEqualTo(100.0);
        assertThat(export.sections()).hasSize(1);
        assertThat(export.sections().get(0).questions()).hasSize(1);
        assertThat(export.sections().get(0).questions().get(0).answer().value()).isEqualTo("Hanoi Heart Hospital");
    }

    @Test
    void exportLeavesAnswerNullForUnansweredQuestions() {
        when(discoveryAnswerRepository.listByProject(project.id)).thenReturn(List.of());
        when(discoveryQuestionRepository.count()).thenReturn(1L);
        when(discoveryAnswerRepository.countByProject(project.id)).thenReturn(0L);

        DiscoverySurveyExport export = discoveryExportService.export(project.id);

        assertThat(export.sections().get(0).questions().get(0).answer()).isNull();
        assertThat(export.progress().percent()).isEqualTo(0.0);
    }
}
