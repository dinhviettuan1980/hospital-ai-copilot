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

import com.hospital.dto.DocumentCategoryRequest;
import com.hospital.dto.DocumentCategoryResponse;
import com.hospital.entity.DocumentCategory;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.DocumentCategoryMapper;
import com.hospital.repository.DocumentCategoryRepository;

class DocumentCategoryServiceTest {

    private DocumentCategoryRepository documentCategoryRepository;
    private DocumentCategoryService documentCategoryService;

    @BeforeEach
    void setUp() {
        documentCategoryRepository = mock(DocumentCategoryRepository.class);
        documentCategoryService = new DocumentCategoryService(documentCategoryRepository, new DocumentCategoryMapper());
    }

    @Test
    void createPersistsNewCategory() {
        when(documentCategoryRepository.existsByName("Policy")).thenReturn(false);

        DocumentCategoryResponse response = documentCategoryService.create(new DocumentCategoryRequest("Policy"));

        assertThat(response.name()).isEqualTo("Policy");
        verify(documentCategoryRepository).persist(any(DocumentCategory.class));
    }

    @Test
    void createRejectsDuplicateName() {
        when(documentCategoryRepository.existsByName("Policy")).thenReturn(true);

        assertThatThrownBy(() -> documentCategoryService.create(new DocumentCategoryRequest("Policy")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void listReturnsSortedCategories() {
        DocumentCategory category = new DocumentCategory();
        category.id = UUID.randomUUID();
        category.name = "SOP";
        when(documentCategoryRepository.listAllSorted()).thenReturn(List.of(category));

        List<DocumentCategoryResponse> result = documentCategoryService.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("SOP");
    }

    @Test
    void findOrThrowThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(documentCategoryRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentCategoryService.findOrThrow(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
