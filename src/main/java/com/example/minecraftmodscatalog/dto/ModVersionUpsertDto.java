package com.example.minecraftmodscatalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO for creating or updating a mod version")
public class ModVersionUpsertDto {
    @NotBlank(message = "Version name is required")
    @Size(min = 1, max = 100, message = "Version name must be between 1 and 100 characters")
    @Schema(description = "Version name", example = "1.0.0")
    private String versionName;

    @Min(value = 0, message = "Download count must be non-negative")
    @Schema(description = "Number of downloads", example = "1500")
    private int downloadCount;

    @NotNull(message = "Mod ID is required")
    @Min(value = 1, message = "Mod ID must be positive")
    @Schema(description = "ID of the mod", example = "1")
    private Long modId;
}
