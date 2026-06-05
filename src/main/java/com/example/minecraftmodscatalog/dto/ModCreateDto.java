package com.example.minecraftmodscatalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "DTO for creating a Minecraft mod")
public class ModCreateDto {
    @NotBlank(message = "Mod name is required")
    @Size(min = 1, max = 255, message = "Mod name must be between 1 and 255 characters")
    @Schema(description = "Name of the mod", example = "Forge")
    private String name;

    @NotBlank(message = "Mod description is required")
    @Size(min = 1, max = 1000, message = "Description must be between 1 and 1000 characters")
    @Schema(description = "Description of the mod", example = "Minecraft Automation Server")
    private String description;

    @NotBlank(message = "Author name is required")
    @Size(min = 1, max = 255, message = "Author name must be between 1 and 255 characters")
    @Schema(description = "Name of the mod author", example = "John Doe")
    private String authorName;

    @Schema(description = "Category name (deprecated, use categoryNames instead)", example = "Utility")
    private String categoryName;

    @Schema(description = "List of category names", example = "[\"Utility\", \"Automation\"]")
    private List<String> categoryNames;

    @Schema(description = "List of tag names", example = "[\"automation\", \"tech\"]")
    private List<String> tagNames;

    @NotEmpty(message = "At least one mod version is required")
    @Valid
    @Schema(description = "List of mod versions")
    private List<ModVersionCreateDto> versions;
}
