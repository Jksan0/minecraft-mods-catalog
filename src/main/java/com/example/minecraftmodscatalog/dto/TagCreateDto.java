package com.example.minecraftmodscatalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO for creating a tag")
public class TagCreateDto {
    @NotBlank(message = "Tag name is required")
    @Size(min = 1, max = 100, message = "Tag name must be between 1 and 100 characters")
    @Schema(description = "Name of the tag", example = "automation")
    private String name;
}
