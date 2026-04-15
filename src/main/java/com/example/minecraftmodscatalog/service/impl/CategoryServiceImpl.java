package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.CategoryCreateDto;
import com.example.minecraftmodscatalog.dto.CategoryDto;
import com.example.minecraftmodscatalog.entity.Category;
import com.example.minecraftmodscatalog.mapper.CategoryMapper;
import com.example.minecraftmodscatalog.repository.CategoryRepository;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private static final String CATEGORY_NOT_FOUND_PREFIX = "Category not found: ";
    private static final String CATEGORY_NAME_ALREADY_EXISTS_PREFIX = "Category name already exists: ";
    private static final String CATEGORY_IS_USED_PREFIX = "Cannot delete category, it is used by mods: ";

    private final CategoryRepository categoryRepository;
    private final ModRepository modRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(final Long id) {
        return categoryRepository.findById(id)
                .map(CategoryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND_PREFIX + id));
    }

    @Override
    @Transactional
    public CategoryDto createCategory(final CategoryCreateDto createDto) {
        validateName(createDto.getName());
        if (categoryRepository.existsByNameIgnoreCase(createDto.getName())) {
            throw new IllegalArgumentException(CATEGORY_NAME_ALREADY_EXISTS_PREFIX + createDto.getName());
        }

        Category category = new Category();
        category.setName(createDto.getName().trim());
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(final Long id, final CategoryCreateDto updateDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND_PREFIX + id));
        validateName(updateDto.getName());
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(updateDto.getName(), id)) {
            throw new IllegalArgumentException(CATEGORY_NAME_ALREADY_EXISTS_PREFIX + updateDto.getName());
        }
        category.setName(updateDto.getName().trim());
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(final Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException(CATEGORY_NOT_FOUND_PREFIX + id);
        }
        if (modRepository.existsByCategoryId(id)) {
            throw new IllegalStateException(CATEGORY_IS_USED_PREFIX + id);
        }
        categoryRepository.deleteById(id);
    }

    private void validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
    }
}
