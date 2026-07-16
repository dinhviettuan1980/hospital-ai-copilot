package com.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.hibernate.orm.panache.PanacheQuery;

import com.hospital.dto.DiscoveryProjectRequest;
import com.hospital.dto.DiscoveryProjectResponse;
import com.hospital.entity.DiscoveryProject;
import com.hospital.entity.DiscoveryProjectStatus;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.DiscoveryProjectMapper;
import com.hospital.repository.DiscoveryAnswerRepository;
import com.hospital.repository.DiscoveryProjectRepository;
import com.hospital.repository.DiscoveryQuestionRepository;

class DiscoveryProjectServiceTest {

    private DiscoveryProjectRepository discoveryProjectRepository;
    private DiscoveryQuestionRepository discoveryQuestionRepository;
    private DiscoveryAnswerRepository discoveryAnswerRepository;
    private DiscoveryAttachmentService discoveryAttachmentService;
    private DiscoveryProjectService discoveryProjectService;

    @BeforeEach
    void setUp() {
        discoveryProjectRepository = mock(DiscoveryProjectRepository.class);
        discoveryQuestionRepository = mock(DiscoveryQuestionRepository.class);
        discoveryAnswerRepository = mock(DiscoveryAnswerRepository.class);
        discoveryAttachmentService = mock(DiscoveryAttachmentService.class);
        DiscoveryProgressCalculator progressCalculator = new DiscoveryProgressCalculator(discoveryQuestionRepository,
                discoveryAnswerRepository);
        discoveryProjectService = new DiscoveryProjectService(discoveryProjectRepository, discoveryAnswerRepository,
                discoveryAttachmentService, new DiscoveryProjectMapper(), progressCalculator);
    }

    private DiscoveryProjectRequest sampleRequest() {
        return new DiscoveryProjectRequest("Discovery Project", "Test Hospital", "Jane Doe",
                "jane@example.test", "555-0100", LocalDate.now(), DiscoveryProjectStatus.DRAFT, "Notes");
    }

    @Test
    void createPersistsProjectWithZeroProgress() {
        DiscoveryProjectResponse response = discoveryProjectService.create(sampleRequest());

        assertThat(response.projectName()).isEqualTo("Discovery Project");
        assertThat(response.progressPercent()).isEqualTo(0.0);
        verify(discoveryProjectRepository).persist(any(DiscoveryProject.class));
    }

    @Test
    void getComputesProgressFromAnsweredOverTotalQuestions() {
        UUID id = UUID.randomUUID();
        DiscoveryProject project = new DiscoveryProject();
        project.id = id;
        project.projectName = "Discovery Project";
        when(discoveryProjectRepository.findByIdOptional(id)).thenReturn(Optional.of(project));
        when(discoveryQuestionRepository.count()).thenReturn(200L);
        when(discoveryAnswerRepository.countByProject(id)).thenReturn(50L);

        DiscoveryProjectResponse response = discoveryProjectService.get(id);

        assertThat(response.progressPercent()).isEqualTo(25.0);
    }

    @Test
    void getThrowsWhenProjectDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(discoveryProjectRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> discoveryProjectService.get(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteCascadesAnswersAndAttachmentsBeforeRemovingProject() {
        UUID id = UUID.randomUUID();
        DiscoveryProject project = new DiscoveryProject();
        project.id = id;
        when(discoveryProjectRepository.findByIdOptional(id)).thenReturn(Optional.of(project));

        discoveryProjectService.delete(id);

        // Answers/attachments reference the project via a non-cascading FK, so both
        // must be cleared before the project row — otherwise Postgres rejects the delete.
        verify(discoveryAnswerRepository).delete("project.id", id);
        verify(discoveryAttachmentService).deleteAllForProject(id);
        verify(discoveryProjectRepository).delete(project);
    }

    @SuppressWarnings("unchecked")
    @Test
    void listMapsPagedResultsWithProgress() {
        DiscoveryProject project = new DiscoveryProject();
        project.id = UUID.randomUUID();
        project.projectName = "Discovery Project";

        PanacheQuery<DiscoveryProject> panacheQuery = mock(PanacheQuery.class);
        when(panacheQuery.list()).thenReturn(List.of(project));
        when(panacheQuery.count()).thenReturn(1L);
        when(discoveryProjectRepository.search(any(), any(), any())).thenReturn(panacheQuery);
        when(discoveryQuestionRepository.count()).thenReturn(10L);
        when(discoveryAnswerRepository.countByProject(project.id)).thenReturn(5L);

        var result = discoveryProjectService.list("discovery", 0, 20, "projectName", "asc");

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).progressPercent()).isEqualTo(50.0);
    }
}
