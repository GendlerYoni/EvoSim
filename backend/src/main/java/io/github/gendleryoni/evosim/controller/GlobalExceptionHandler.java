
package io.github.gendleryoni.evosim.controller;

import io.github.gendleryoni.evosim.dto.ApiErrorResponse;
import io.github.gendleryoni.evosim.service.ServerAtCapacityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ServerAtCapacityException.class)
    public ResponseEntity<ApiErrorResponse> handleCapacity(
            ServerAtCapacityException exception
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                "SERVER_AT_CAPACITY",
                exception.getMessage(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError error :
                exception.getBindingResult().getFieldErrors()) {

            fieldErrors.putIfAbsent(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        ApiErrorResponse response = new ApiErrorResponse(
                "VALIDATION_ERROR",
                "Invalid simulation configuration",
                fieldErrors
        );

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                "INVALID_REQUEST_BODY",
                "Request body must contain valid JSON with the expected field types",
                Map.of()
        );

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception
    ) {
        if (exception instanceof ErrorResponse errorResponse
                && errorResponse.getStatusCode().is4xxClientError()) {

            ApiErrorResponse response = new ApiErrorResponse(
                    "HTTP_ERROR",
                    "Invalid HTTP request",
                    Map.of()
            );

            return ResponseEntity
                    .status(errorResponse.getStatusCode())
                    .headers(errorResponse.getHeaders())
                    .body(response);
        }

        logger.error(
                "Unexpected error while processing HTTP request",
                exception
        );

        ApiErrorResponse response = new ApiErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected server error occurred",
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
