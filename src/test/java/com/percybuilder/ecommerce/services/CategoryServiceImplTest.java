package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.CategoryRequest;
import com.percybuilder.ecommerce.dtos.CategoryResponse;
import com.percybuilder.ecommerce.exceptions.BadRequestException;
import com.percybuilder.ecommerce.exceptions.ResourceNotFoundException;
import com.percybuilder.ecommerce.models.Category;
import com.percybuilder.ecommerce.repositories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_shouldCreateCategory_whenNameDoesNotExist() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Perfumes");
        request.setDescription("Luxury fragrance products");

        Category savedCategory = Category.builder()
                .id(1L)
                .name("Perfumes")
                .description("Luxury fragrance products")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Perfumes")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Perfumes");
        assertThat(response.getDescription()).isEqualTo("Luxury fragrance products");

        verify(categoryRepository).existsByNameIgnoreCase("Perfumes");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_shouldThrowBadRequest_whenNameAlreadyExists() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Perfumes");
        request.setDescription("Duplicate category");

        when(categoryRepository.existsByNameIgnoreCase("Perfumes")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Category already exists with name: Perfumes");

        verify(categoryRepository).existsByNameIgnoreCase("Perfumes");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategoryById_shouldReturnCategory_whenCategoryExists() {
        Category category = Category.builder()
                .id(1L)
                .name("Perfumes")
                .description("Luxury fragrance products")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getCategoryById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Perfumes");

        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_shouldThrowNotFound_whenCategoryDoesNotExist() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 999");

        verify(categoryRepository).findById(999L);
    }
}