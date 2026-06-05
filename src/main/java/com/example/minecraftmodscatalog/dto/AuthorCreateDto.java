package com.example.minecraftmodscatalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO for creating an author")
public class AuthorCreateDto {
    @NotBlank(message = "Author name is required")
    @Size(min = 1, max = 255, message = "Author name must be between 1 and 255 characters")
    @Schema(description = "Name of the author", example = "John Doe")
    private String name;
}
