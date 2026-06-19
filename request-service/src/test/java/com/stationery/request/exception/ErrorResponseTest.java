package com.stationery.request.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    // ─── ErrorResponse construction ──────────────────────────────────────────

    @Test
    void errorResponse_defaultConstructor_allFieldsAreDefaultValues() {
        ErrorResponse response = new ErrorResponse();

        assertThat(response.getStatus()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getTimestamp()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void errorResponse_setters_workCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> errors = Map.of("field", "must not be blank");

        ErrorResponse response = new ErrorResponse();
        response.setStatus(400);
        response.setMessage("Validation failed");
        response.setTimestamp(now);
        response.setErrors(errors);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.getErrors()).containsEntry("field", "must not be blank");
    }

    @Test
    void errorResponse_builder_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> errors = Map.of("items", "must not be empty");

        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .message("Validation failed")
                .timestamp(now)
                .errors(errors)
                .build();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void errorResponse_404_noErrors_mapIsNull() {
        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .message("Request not found with id: 99")
                .timestamp(LocalDateTime.now())
                .errors(null)
                .build();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).contains("99");
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void errorResponse_500_noErrors_mapIsNull() {
        ErrorResponse response = ErrorResponse.builder()
                .status(500)
                .message("Unexpected error occurred")
                .timestamp(LocalDateTime.now())
                .errors(null)
                .build();

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getMessage()).isEqualTo("Unexpected error occurred");
    }

    @Test
    void errorResponse_400_withMultipleValidationErrors() {
        Map<String, String> errors = Map.of(
                "items", "must not be empty",
                "studentUsername", "must not be blank"
        );

        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .message("Validation failed")
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors()).containsKey("items");
        assertThat(response.getErrors()).containsKey("studentUsername");
    }

    @Test
    void errorResponse_timestamp_isStoredExactly() {
        LocalDateTime fixed = LocalDateTime.of(2024, 6, 15, 10, 30, 0);

        ErrorResponse response = ErrorResponse.builder()
                .status(200)
                .message("ok")
                .timestamp(fixed)
                .errors(null)
                .build();

        assertThat(response.getTimestamp()).isEqualTo(fixed);
    }

    @Test
    void errorResponse_emptyErrorsMap_isDistinctFromNull() {
        ErrorResponse withEmpty = ErrorResponse.builder()
                .status(400)
                .message("Bad request")
                .timestamp(LocalDateTime.now())
                .errors(Map.of())
                .build();

        ErrorResponse withNull = ErrorResponse.builder()
                .status(400)
                .message("Bad request")
                .timestamp(LocalDateTime.now())
                .errors(null)
                .build();

        assertThat(withEmpty.getErrors()).isNotNull().isEmpty();
        assertThat(withNull.getErrors()).isNull();
    }

    // ─── ResourceNotFoundException ───────────────────────────────────────────

    @Test
    void resourceNotFoundException_withLongId_formatsMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Request", "id", 42L);

        assertThat(ex.getMessage()).contains("Request");
        assertThat(ex.getMessage()).contains("id");
        assertThat(ex.getMessage()).contains("42");
    }

    @Test
    void resourceNotFoundException_withStringId_formatsMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Request", "requestId", "req-uuid-001");

        assertThat(ex.getMessage()).contains("Request");
        assertThat(ex.getMessage()).contains("requestId");
        assertThat(ex.getMessage()).contains("req-uuid-001");
    }

    @Test
    void resourceNotFoundException_isRuntimeException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Item", "id", 1L);

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    // ─── InsufficientStockException ──────────────────────────────────────────

    @Test
    void insufficientStockException_formatsMessageWithItemAndQuantity() {
        InsufficientStockException ex = new InsufficientStockException("Pen", 10);

        assertThat(ex.getMessage()).contains("Pen");
        assertThat(ex.getMessage()).contains("10");
    }

    @Test
    void insufficientStockException_isRuntimeException() {
        InsufficientStockException ex = new InsufficientStockException("Notebook", 5);

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void insufficientStockException_differentItems_haveDistinctMessages() {
        InsufficientStockException ex1 = new InsufficientStockException("Pen", 3);
        InsufficientStockException ex2 = new InsufficientStockException("Ruler", 7);

        assertThat(ex1.getMessage()).contains("Pen").contains("3");
        assertThat(ex2.getMessage()).contains("Ruler").contains("7");
        assertThat(ex1.getMessage()).isNotEqualTo(ex2.getMessage());
    }
}