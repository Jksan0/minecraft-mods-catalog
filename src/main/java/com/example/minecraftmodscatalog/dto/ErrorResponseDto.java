package com.example.minecraftmodscatalog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Unified error response format")
public class ErrorResponseDto {
    @Schema(description = "Human-readable error message", example = "Validation failed")
    private String message;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP status reason phrase", example = "Bad Request")
    private String error;

    @Schema(description = "Request path", example = "/api/mods")
    private String path;

    @Schema(description = "Timestamp when the error occurred")
    private LocalDateTime timestamp;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Field-level validation errors")
    private List<FieldErrorDto> fieldErrors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Field validation error details")
    public static class FieldErrorDto {
        @Schema(description = "Field name", example = "name")
        private String field;

        @Schema(description = "Validation error message", example = "Mod name is required")
        private String message;

        @Schema(description = "Rejected value")
        private Object rejectedValue;
    }
}
