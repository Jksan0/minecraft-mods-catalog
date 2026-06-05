package com.example.minecraftmodscatalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO for category response")
public class CategoryDto {
    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Utility")
    private String name;
}
