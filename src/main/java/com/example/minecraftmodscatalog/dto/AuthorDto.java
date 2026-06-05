package com.example.minecraftmodscatalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO for author response")
public class AuthorDto {
    @Schema(description = "Author ID", example = "1")
    private Long id;

    @Schema(description = "Author name", example = "John Doe")
    private String name;
}
