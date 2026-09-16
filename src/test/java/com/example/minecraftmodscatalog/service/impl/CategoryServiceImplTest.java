package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.CategoryCreateDto;
import com.example.minecraftmodscatalog.entity.Category;
import com.example.minecraftmodscatalog.repository.CategoryRepository;
import com.example.minecraftmodscatalog.repository.ModRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModRepository modRepository;

    @InjectMocks
    private CategoryServiceImpl service;

    @Test
    void createCategory_shouldSaveTrimmedName() {
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("  Utility  ");
        Category saved = new Category();
        saved.setId(2L);
        saved.setName("Utility");

        when(categoryRepository.existsByNameIgnoreCase("  Utility  ")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        var result = service.createCategory(dto);

        assertThat(result.getName()).isEqualTo("Utility");
    }

    @Test
    void deleteCategory_shouldThrowIfUsedByMods() {
        when(categoryRepository.existsById(5L)).thenReturn(true);
        when(modRepository.existsByCategoryId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteCategory(5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("used by mods");
    }
}
