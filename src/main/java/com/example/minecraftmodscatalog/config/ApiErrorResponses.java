package com.example.minecraftmodscatalog.config;

import com.example.minecraftmodscatalog.dto.ErrorResponseDto;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
    @ApiResponse(
            responseCode = "400",
            description = "Validation error or bad request",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    ),
    @ApiResponse(
            responseCode = "404",
            description = "Entity not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    ),
    @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    ),
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    )
})
public @interface ApiErrorResponses {
}
