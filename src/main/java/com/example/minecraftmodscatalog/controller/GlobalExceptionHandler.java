package com.example.minecraftmodscatalog.controller;

import com.example.minecraftmodscatalog.dto.ErrorResponseDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(
            final EntityNotFoundException ex,
            final WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.warn("Entity not found: {} - status: {}", ex.getMessage(), status.value());
        return buildResponse(status, ex.getMessage(), request, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(
            final IllegalArgumentException ex,
            final WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn("Illegal argument: {} - status: {}", ex.getMessage(), status.value());
        return buildResponse(status, ex.getMessage(), request, null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleStateConflict(
            final IllegalStateException ex,
            final WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        log.warn("Illegal state: {} - status: {}", ex.getMessage(), status.value());
        return buildResponse(status, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(
            final ConstraintViolationException ex,
            final WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn("Constraint violation: {} error(s) - status: {}",
                ex.getConstraintViolations().size(), status.value());
        List<ErrorResponseDto.FieldErrorDto> fieldErrors = ex.getConstraintViolations().stream()
                .map(violation -> ErrorResponseDto.FieldErrorDto.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build())
                .toList();
        return buildResponse(status, "Validation failed", request, fieldErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(
            final MethodArgumentTypeMismatchException ex,
            final WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Invalid value '%s' for parameter '%s'".formatted(ex.getValue(), ex.getName());
        log.warn("Type mismatch: {} - status: {}", message, status.value());
        return buildResponse(status, message, request, null);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        log.warn("Validation failed: {} error(s) - status: {}",
                ex.getBindingResult().getErrorCount(), httpStatus.value());
        List<ErrorResponseDto.FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorResponseDto.FieldErrorDto.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .toList();

        ErrorResponseDto response = buildErrorResponse(
                httpStatus, "Validation failed", request, fieldErrors);
        return ResponseEntity.status(httpStatus).body(response);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        log.warn("Malformed request body: {} - status: {}", ex.getMessage(), httpStatus.value(), ex);
        ErrorResponseDto response = buildErrorResponse(
                httpStatus, "Malformed request body", request, null);
        return ResponseEntity.status(httpStatus).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            final Exception ex,
            final WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Internal server error - status: {}", status.value(), ex);
        return buildResponse(status, "Internal server error", request, null);
    }

    private ResponseEntity<ErrorResponseDto> buildResponse(
            HttpStatus status,
            String message,
            WebRequest request,
            List<ErrorResponseDto.FieldErrorDto> fieldErrors) {
        return ResponseEntity.status(status).body(buildErrorResponse(status, message, request, fieldErrors));
    }

    private ErrorResponseDto buildErrorResponse(
            HttpStatus status,
            String message,
            WebRequest request,
            List<ErrorResponseDto.FieldErrorDto> fieldErrors) {
        return ErrorResponseDto.builder()
                .message(message)
                .status(status.value())
                .error(status.getReasonPhrase())
                .path(extractPath(request))
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();
    }

    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
