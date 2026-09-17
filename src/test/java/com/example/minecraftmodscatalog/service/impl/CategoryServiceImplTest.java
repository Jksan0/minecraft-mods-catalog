package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.CategoryCreateDto;
import com.example.minecraftmodscatalog.dto.CategoryDto;
import com.example.minecraftmodscatalog.entity.Category;
import com.example.minecraftmodscatalog.repository.CategoryRepository;
import com.example.minecraftmodscatalog.repository.ModRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    void getAllCategories_shouldReturnMappedCategories() {
        Category category = new Category();
        category.setId(3L);
        category.setName("Utility");
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryDto> result = service.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Utility");
    }

    @Test
    void getCategoryById_shouldReturnCategory() {
        Category category = new Category();
        category.setId(7L);
        category.setName("Utility");
        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category));

        CategoryDto result = service.getCategoryById(7L);

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getName()).isEqualTo("Utility");
    }

    @Test
    void getCategoryById_shouldThrowWhenMissing() {
        when(categoryRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCategoryById(77L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found: 77");
    }

    @Test
    void createCategory_shouldSaveTrimmedName() {
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("  Utility  ");
        Category saved = new Category();
        saved.setId(2L);
        saved.setName("Utility");

        when(categoryRepository.existsByNameIgnoreCase("  Utility  ")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryDto result = service.createCategory(dto);

        assertThat(result.getName()).isEqualTo("Utility");
    }

    @Test
    void createCategory_shouldRejectBlankName() {
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName(" ");

        assertThatThrownBy(() -> service.createCategory(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category name is required");
    }

    @Test
    void createCategory_shouldRejectDuplicateName() {
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("Utility");
        when(categoryRepository.existsByNameIgnoreCase("Utility")).thenReturn(true);

        assertThatThrownBy(() -> service.createCategory(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category name already exists: Utility");
    }

    @Test
    void updateCategory_shouldUpdateTrimmedName() {
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("  Forge  ");
        Category category = new Category();
        category.setId(8L);
        category.setName("Old");

        when(categoryRepository.findById(8L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("  Forge  ", 8L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = service.updateCategory(8L, dto);

        assertThat(result.getName()).isEqualTo("Forge");
        assertThat(category.getName()).isEqualTo("Forge");
    }

    @Test
    void updateCategory_shouldRejectDuplicateName() {
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("Utility");
        Category category = new Category();
        category.setId(8L);
        category.setName("Old");

        when(categoryRepository.findById(8L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Utility", 8L)).thenReturn(true);

        assertThatThrownBy(() -> service.updateCategory(8L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category name already exists: Utility");
    }

    @Test
    void deleteCategory_shouldDeleteWhenUnused() {
        when(categoryRepository.existsById(5L)).thenReturn(true);
        when(modRepository.existsByCategoryId(5L)).thenReturn(false);

        service.deleteCategory(5L);

        verify(categoryRepository).deleteById(5L);
    }

    @Test
    void deleteCategory_shouldThrowWhenNotFound() {
        when(categoryRepository.existsById(6L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteCategory(6L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found: 6");
    }

    @Test
    void deleteCategory_shouldThrowIfUsedByMods() {
        when(categoryRepository.existsById(5L)).thenReturn(true);
        when(modRepository.existsByCategoryId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteCategory(5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("used by mods");
    }

    @Test
    void syntheticLambdas_shouldBeInvoked() throws Exception {
        var getCategoryLambda = CategoryServiceImpl.class.getDeclaredMethod("lambda$getCategoryById$0", Long.class);
        getCategoryLambda.setAccessible(true);
        assertThat(getCategoryLambda.invoke(null, 7L)).isInstanceOf(jakarta.persistence.EntityNotFoundException.class);

        var updateCategoryLambda = CategoryServiceImpl.class.getDeclaredMethod("lambda$updateCategory$1", Long.class);
        updateCategoryLambda.setAccessible(true);
        assertThat(updateCategoryLambda.invoke(null, 9L)).isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }
}
