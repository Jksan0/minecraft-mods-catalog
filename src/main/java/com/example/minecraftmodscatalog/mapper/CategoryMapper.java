package com.example.minecraftmodscatalog.mapper;

import com.example.minecraftmodscatalog.dto.CategoryDto;
import com.example.minecraftmodscatalog.entity.Category;

public final class CategoryMapper {
    private CategoryMapper() {
    }

    public static CategoryDto toDto(final Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
