package com.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hospital.dto.DiscoveryAnswerExport;
import com.hospital.dto.DiscoveryProjectExport;
import com.hospital.dto.DiscoveryProjectResponse;
import com.hospital.dto.DiscoveryQuestionExport;
import com.hospital.dto.DiscoverySectionExport;
import com.hospital.dto.DiscoverySurveyExport;
import com.hospital.entity.DiscoveryAnswer;
import com.hospital.entity.DiscoveryAnswerType;
import com.hospital.entity.DiscoveryProjectStatus;
import com.hospital.entity.DiscoveryQuestion;
import com.hospital.entity.DiscoveryRiskLevel;
import com.hospital.exception.InvalidFileException;
import com.hospital.mapper.DiscoveryProjectMapper;
import com.hospital.repository.DiscoveryAnswerRepository;
import com.hospital.repository.DiscoveryProjectRepository;
import com.hospital.repository.DiscoveryQuestionRepository;

class DiscoveryImportServiceTest {

    private DiscoveryProjectRepository discoveryProjectRepository;
    private DiscoveryQuestionRepository discoveryQuestionRepository;
    private DiscoveryAnswerRepository discoveryAnswerRepository;
    private DiscoveryImportService discoveryImportService;

    @BeforeEach
    void setUp() {
        discoveryProjectRepository = mock(DiscoveryProjectRepository.class);
        discoveryQuestionRepository = mock(DiscoveryQuestionRepository.class);
        discoveryAnswerRepository = mock(DiscoveryAnswerRepository.class);
        discoveryImportService = new DiscoveryImportService(discoveryProjectRepository, discoveryQuestionRepository,
                discoveryAnswerRepository, new DiscoveryProjectMapper(),
                new DiscoveryProgressCalculator(discoveryQuestionRepository, discoveryAnswerRepository));
    }

    private DiscoverySurveyExport sampleExport(DiscoveryAnswerExport answer) {
        DiscoveryQuestionExport question = new DiscoveryQuestionExport("OVERVIEW-01", "What is the hospital name?",
                null, DiscoveryAnswerType.TEXT, List.of(), answer, List.of());
        DiscoverySectionExport section = new DiscoverySectionExport("OVERVIEW", "Hospital Overview", null, 1,
                List.of(question));
        DiscoveryProjectExport project = new DiscoveryProjectExport("Imported Project", "Imported Hospital", "Jane",
                "jane@example.test", "555-0100", LocalDate.now(), DiscoveryProjectStatus.COMPLETED, "notes");
        return new DiscoverySurveyExport(1, java.time.Instant.now(), project, null, List.of(section));
    }

    @Test
    void importCreatesNewProjectAndMatchingAnswer() {
        DiscoveryQuestion question = new DiscoveryQuestion();
        question.id = UUID.randomUUID();
        question.code = "OVERVIEW-01";
        when(discoveryQuestionRepository.listAll()).thenReturn(List.of(question));

        DiscoverySurveyExport export = sampleExport(new DiscoveryAnswerExport("Hanoi Heart Hospital", "comment",
                DiscoveryRiskLevel.MEDIUM));

        DiscoveryProjectResponse response = discoveryImportService.importSurvey(export);

        assertThat(response.projectName()).isEqualTo("Imported Project");
        verify(discoveryProjectRepository).persist(any(com.hospital.entity.DiscoveryProject.class));
        verify(discoveryAnswerRepository).persist(any(DiscoveryAnswer.class));
    }

    @Test
    void importSkipsQuestionsWhoseCodeNoLongerExistsInTheCatalog() {
        when(discoveryQuestionRepository.listAll()).thenReturn(List.of());

        DiscoverySurveyExport export = sampleExport(new DiscoveryAnswerExport("value", null, null));

        discoveryImportService.importSurvey(export);

        verify(discoveryAnswerRepository, never()).persist(any(DiscoveryAnswer.class));
    }

    @Test
    void importSkipsQuestionsWithNoAnswerInTheExport() {
        DiscoverySurveyExport export = sampleExport(null);

        discoveryImportService.importSurvey(export);

        verify(discoveryAnswerRepository, never()).persist(any(DiscoveryAnswer.class));
    }

    @Test
    void importRejectsMalformedExport() {
        assertThatThrownBy(() -> discoveryImportService.importSurvey(null))
                .isInstanceOf(InvalidFileException.class);

        DiscoverySurveyExport missingProject = new DiscoverySurveyExport(1, java.time.Instant.now(), null, null,
                List.of());
        assertThatThrownBy(() -> discoveryImportService.importSurvey(missingProject))
                .isInstanceOf(InvalidFileException.class);
    }
}
