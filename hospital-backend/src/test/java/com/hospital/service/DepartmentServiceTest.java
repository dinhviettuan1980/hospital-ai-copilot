package com.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.hibernate.orm.panache.PanacheQuery;

import com.hospital.dto.DepartmentRequest;
import com.hospital.dto.DepartmentResponse;
import com.hospital.entity.Department;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.DepartmentMapper;
import com.hospital.repository.DepartmentRepository;

class DepartmentServiceTest {

    private DepartmentRepository departmentRepository;
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        departmentRepository = mock(DepartmentRepository.class);
        // Use the real mapper: it is pure data transformation with no dependencies worth mocking.
        departmentService = new DepartmentService(departmentRepository, new DepartmentMapper());
    }

    @Test
    void createPersistsDepartmentWhenCodeAndNameAreUnique() {
        DepartmentRequest request = new DepartmentRequest("Cardiology", "CARD", "Heart care");
        when(departmentRepository.existsByCode("CARD")).thenReturn(false);
        when(departmentRepository.existsByName("Cardiology")).thenReturn(false);

        DepartmentResponse response = departmentService.create(request);

        assertThat(response.name()).isEqualTo("Cardiology");
        assertThat(response.code()).isEqualTo("CARD");
        verify(departmentRepository).persist(any(Department.class));
    }

    @Test
    void createRejectsDuplicateCode() {
        DepartmentRequest request = new DepartmentRequest("Cardiology", "CARD", "Heart care");
        when(departmentRepository.existsByCode("CARD")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CARD");
    }

    @Test
    void createRejectsDuplicateName() {
        DepartmentRequest request = new DepartmentRequest("Cardiology", "CARD", "Heart care");
        when(departmentRepository.existsByCode("CARD")).thenReturn(false);
        when(departmentRepository.existsByName("Cardiology")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Cardiology");
    }

    @Test
    void getThrowsWhenDepartmentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(departmentRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.get(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void getReturnsMappedDepartmentWhenFound() {
        Department department = new Department();
        department.id = UUID.randomUUID();
        department.name = "Emergency";
        department.code = "ER";
        when(departmentRepository.findByIdOptional(department.id)).thenReturn(Optional.of(department));

        DepartmentResponse response = departmentService.get(department.id);

        assertThat(response.id()).isEqualTo(department.id);
        assertThat(response.name()).isEqualTo("Emergency");
    }

    @Test
    void updateRejectsCodeAlreadyUsedByAnotherDepartment() {
        UUID id = UUID.randomUUID();
        Department existing = new Department();
        existing.id = id;
        existing.name = "Emergency";
        existing.code = "ER";
        when(departmentRepository.findByIdOptional(id)).thenReturn(Optional.of(existing));

        Department other = new Department();
        other.id = UUID.randomUUID();
        other.code = "CARD";
        when(departmentRepository.findByCode("CARD")).thenReturn(Optional.of(other));

        DepartmentRequest request = new DepartmentRequest("Emergency", "CARD", "desc");

        assertThatThrownBy(() -> departmentService.update(id, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void deleteRemovesExistingDepartment() {
        UUID id = UUID.randomUUID();
        Department existing = new Department();
        existing.id = id;
        when(departmentRepository.findByIdOptional(id)).thenReturn(Optional.of(existing));

        departmentService.delete(id);

        verify(departmentRepository).delete(existing);
    }

    @Test
    void deleteThrowsWhenDepartmentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(departmentRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void listMapsPagedResultsFromRepository() {
        Department department = new Department();
        department.id = UUID.randomUUID();
        department.name = "Emergency";
        department.code = "ER";

        PanacheQuery<Department> panacheQuery = mock(PanacheQuery.class);
        when(panacheQuery.list()).thenReturn(List.of(department));
        when(panacheQuery.count()).thenReturn(1L);
        when(departmentRepository.search(any(), any(), any())).thenReturn(panacheQuery);

        var result = departmentService.list("emer", 0, 20, "name", "asc");

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content().get(0).name()).isEqualTo("Emergency");
    }
}
