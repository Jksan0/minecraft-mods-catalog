package com.example.minecraftmodscatalog.service;

import com.example.minecraftmodscatalog.dto.CategoryCreateDto;
import com.example.minecraftmodscatalog.dto.CategoryDto;
import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAllCategories();

    CategoryDto getCategoryById(Long id);

    CategoryDto createCategory(CategoryCreateDto createDto);

    CategoryDto updateCategory(Long id, CategoryCreateDto updateDto);

    void deleteCategory(Long id);
}
