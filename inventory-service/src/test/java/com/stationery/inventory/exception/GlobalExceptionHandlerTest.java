package com.stationery.inventory.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFoundException_returns404WithMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Item not found with id: 42");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Item not found with id: 42");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleResourceNotFoundException_withCause_returns404() {
        Throwable cause = new RuntimeException("root cause");
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found", cause);

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Not found");
    }

    @Test
    void handleInsufficientStockException_returns400WithMessage() {
        InsufficientStockException ex = new InsufficientStockException("Insufficient stock for item ID: 1");

        ResponseEntity<ErrorResponse> response = handler.handleInsufficientStockException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Insufficient stock for item ID: 1");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleInsufficientStockException_withCause_returns400() {
        Throwable cause = new RuntimeException("db error");
        InsufficientStockException ex = new InsufficientStockException("Out of stock", cause);

        ResponseEntity<ErrorResponse> response = handler.handleInsufficientStockException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Out of stock");
    }

    @Test
    void handleMethodArgumentNotValidException_returns400WithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError nameError = new FieldError("request", "name", "Item name is required");
        FieldError qtyError = new FieldError("request", "availableQuantity", "Available quantity is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(nameError, qtyError));

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("Validation failed");
        assertThat(response.getBody().getMessage()).contains("name: Item name is required");
        assertThat(response.getBody().getMessage()).contains("availableQuantity: Available quantity is required");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleMethodArgumentNotValidException_singleError_returns400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError error = new FieldError("request", "category", "Category is required");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error));

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("category: Category is required");
    }

    @Test
    void handleGenericException_returns500WithMessage() {
        Exception ex = new RuntimeException("Unexpected database failure");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).contains("An unexpected error occurred");
        assertThat(response.getBody().getMessage()).contains("Unexpected database failure");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleGenericException_withNullMessage_returns500() {
        Exception ex = new RuntimeException((String) null);

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }

    // ===== ErrorResponse builder/getter/setter coverage =====

    @Test
    void errorResponse_builderAndGetters_work() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(404)
                .message("Not found")
                .timestamp(now)
                .build();

        assertThat(errorResponse.getStatus()).isEqualTo(404);
        assertThat(errorResponse.getMessage()).isEqualTo("Not found");
        assertThat(errorResponse.getTimestamp()).isEqualTo(now);
    }

    @Test
    void errorResponse_setters_work() {
        ErrorResponse errorResponse = new ErrorResponse();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        errorResponse.setStatus(500);
        errorResponse.setMessage("Error");
        errorResponse.setTimestamp(now);

        assertThat(errorResponse.getStatus()).isEqualTo(500);
        assertThat(errorResponse.getMessage()).isEqualTo("Error");
        assertThat(errorResponse.getTimestamp()).isEqualTo(now);
    }

    @Test
    void errorResponse_allArgsConstructor_works() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        ErrorResponse errorResponse = new ErrorResponse(400, "Bad request", now);

        assertThat(errorResponse.getStatus()).isEqualTo(400);
        assertThat(errorResponse.getMessage()).isEqualTo("Bad request");
        assertThat(errorResponse.getTimestamp()).isEqualTo(now);
    }

    // ===== Exception class coverage =====

    @Test
    void resourceNotFoundException_messageConstructor_storesMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        assertThat(ex.getMessage()).isEqualTo("Resource not found");
    }

    @Test
    void resourceNotFoundException_causeConstructor_storesBoth() {
        Throwable cause = new IllegalStateException("cause");
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found", cause);
        assertThat(ex.getMessage()).isEqualTo("Not found");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void insufficientStockException_messageConstructor_storesMessage() {
        InsufficientStockException ex = new InsufficientStockException("Out of stock");
        assertThat(ex.getMessage()).isEqualTo("Out of stock");
    }

    @Test
    void insufficientStockException_causeConstructor_storesBoth() {
        Throwable cause = new IllegalStateException("cause");
        InsufficientStockException ex = new InsufficientStockException("Not enough", cause);
        assertThat(ex.getMessage()).isEqualTo("Not enough");
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}