package com.example.minecraftmodscatalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "DTO for mod response")
public class ModDto {
    @Schema(description = "Mod ID", example = "1")
    private Long id;

    @Schema(description = "Mod name", example = "Forge")
    private String name;

    @Schema(description = "Mod description", example = "Minecraft Automation Server")
    private String description;

    @Schema(description = "Author name", example = "John Doe")
    private String authorName;

    @Schema(description = "Category name", example = "Utility")
    private String categoryName;

    @Schema(description = "List of tag names", example = "[\"automation\", \"tech\"]")
    private List<String> tags;

    @Schema(description = "List of mod versions")
    private List<ModVersionDto> versions;
}
