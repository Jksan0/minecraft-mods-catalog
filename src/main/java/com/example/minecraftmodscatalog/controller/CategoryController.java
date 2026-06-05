package com.example.minecraftmodscatalog.controller;

import com.example.minecraftmodscatalog.config.ApiErrorResponses;
import com.example.minecraftmodscatalog.dto.CategoryCreateDto;
import com.example.minecraftmodscatalog.dto.CategoryDto;
import com.example.minecraftmodscatalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management API")
@ApiErrorResponses
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable final Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody final CategoryCreateDto createDto) {
        return ResponseEntity.status(201).body(categoryService.createCategory(createDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category by ID")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable final Long id,
            @Valid @RequestBody final CategoryCreateDto updateDto
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(id, updateDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category by ID")
    public ResponseEntity<Void> deleteCategory(@PathVariable final Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
