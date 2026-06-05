package com.example.minecraftmodscatalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO for mod version response")
public class ModVersionDto {
    @Schema(description = "Version ID", example = "1")
    private Long id;

    @Schema(description = "Version name", example = "1.0.0")
    private String versionName;

    @Schema(description = "Number of downloads", example = "1500")
    private int downloadCount;
}
