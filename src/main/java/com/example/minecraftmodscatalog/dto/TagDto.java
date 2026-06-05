package com.example.minecraftmodscatalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO for tag response")
public class TagDto {
    @Schema(description = "Tag ID", example = "1")
    private Long id;

    @Schema(description = "Tag name", example = "automation")
    private String name;
}
