package com.stationery.auth.exception;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for standardizing API error responses.
 * 
 * WHY IT EXISTS:
 * When an error occurs (e.g., bad credentials, validation failure), we don't want to return 
 * an ugly HTML stack trace to the client. We want to return a clean, predictable JSON format 
 * so the frontend can easily parse it and display a helpful message to the user.
 * 
 * ARCHITECTURE:
 * This acts as the standard contract for all error responses across the application, 
 * populated and returned by the GlobalExceptionHandler.
 */
public class ErrorResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse() {}

    public ErrorResponse(int status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static ErrorResponseBuilder builder() { return new ErrorResponseBuilder(); }

    public static class ErrorResponseBuilder {
        private int status;
        private String message;
        private LocalDateTime timestamp;

        public ErrorResponseBuilder status(int status) { this.status = status; return this; }
        public ErrorResponseBuilder message(String message) { this.message = message; return this; }
        public ErrorResponseBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public ErrorResponse build() {
            return new ErrorResponse(status, message, timestamp);
        }
    }
}
