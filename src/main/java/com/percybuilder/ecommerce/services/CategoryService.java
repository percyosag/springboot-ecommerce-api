package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.CategoryRequest;
import com.percybuilder.ecommerce.dtos.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest categoryRequest);

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest);

    void deleteCategory(Long id);
}