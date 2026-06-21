package com.stationery.request.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stationery.request.controller.RequestController;
import com.stationery.request.dto.CreateRequestDto;
import com.stationery.request.service.RequestService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.codec.DecodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    private MockMvc mockMvc;
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(requestController)
                .setControllerAdvice(handler)
                .setMessageConverters(converter)
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Builds a minimal Feign Request required by FeignException subclass constructors.
     */
    private Request dummyFeignRequest() {
        return Request.create(
                Request.HttpMethod.PUT,
                "http://inventory-service/api/inventory/1/deduct",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );
    }

    /**
     * Builds a MethodArgumentNotValidException with one rejected field, using
     * a zero-parameter method on this class as the MethodParameter handle so
     * no reflection on production classes is needed.
     */
    private MethodArgumentNotValidException buildValidationException(
            String field, String code, String defaultMessage) throws NoSuchMethodException {

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new CreateRequestDto(), "createRequestDto");
        bindingResult.rejectValue(field, code, defaultMessage);

        return new MethodArgumentNotValidException(
                new MethodParameter(
                        GlobalExceptionHandlerTest.class.getDeclaredMethod("setUp"), -1),
                bindingResult);
    }

    // ─── handleResourceNotFoundException ─────────────────────────────────────

    @Test
    void handleResourceNotFoundException_singleMessage_returns404() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Request not found with id: 42");

        ResponseEntity<ErrorResponse> response =
                handler.handleResourceNotFoundException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Request not found with id: 42");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrors()).isNull();
    }

    @Test
    void handleResourceNotFoundException_threeArgConstructor_messageContainsAllParts() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Request", "id", 99L);

        ResponseEntity<ErrorResponse> response =
                handler.handleResourceNotFoundException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("Request");
        assertThat(response.getBody().getMessage()).contains("id");
        assertThat(response.getBody().getMessage()).contains("99");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrors()).isNull();
    }

    @Test
    void handleResourceNotFoundException_threeArgConstructor_stringFieldValue() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Request", "requestId", "req-uuid-001");

        ResponseEntity<ErrorResponse> response =
                handler.handleResourceNotFoundException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("requestId");
        assertThat(response.getBody().getMessage()).contains("req-uuid-001");
    }

    @Test
    void handleResourceNotFoundException_viaMockMvc_returns404WithBody() throws Exception {
        when(requestService.getRequestById(99L))
                .thenThrow(new ResourceNotFoundException("Request", "id", 99L));

        mockMvc.perform(get("/api/requests/99")
                        .header("X-User-Name", "student1")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─── handleInsufficientStockException ────────────────────────────────────

    @Test
    void handleInsufficientStockException_singleMessage_returns400() {
        InsufficientStockException ex =
                new InsufficientStockException("Insufficient stock for item: Pen");

        ResponseEntity<ErrorResponse> response =
                handler.handleInsufficientStockException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Insufficient stock for item: Pen");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrors()).isNull();
    }

    @Test
    void handleInsufficientStockException_twoArgConstructor_messageContainsItemAndQuantity() {
        InsufficientStockException ex =
                new InsufficientStockException("Notebook", 5);

        ResponseEntity<ErrorResponse> response =
                handler.handleInsufficientStockException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        // Exact format from InsufficientStockException:
        // "Insufficient stock for item 'Notebook'. Requested quantity: 5"
        assertThat(response.getBody().getMessage()).contains("Notebook");
        assertThat(response.getBody().getMessage()).contains("5");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrors()).isNull();
    }

    @Test
    void handleInsufficientStockException_viaMockMvc_returns400WithBody() throws Exception {
        when(requestService.approveRequest(1L, "admin"))
                .thenThrow(new InsufficientStockException("Pen", 10));

        mockMvc.perform(put("/api/requests/1/approve")
                        .header("X-User-Name", "admin")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─── handleMethodArgumentNotValidException ────────────────────────────────

    @Test
    void handleMethodArgumentNotValidException_singleFieldError_returns400WithErrorsMap()
            throws Exception {
        MethodArgumentNotValidException ex =
                buildValidationException("items", "NotEmpty", "must not be empty");

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentNotValidException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrors()).isNotNull();
        assertThat(response.getBody().getErrors()).containsKey("items");
        assertThat(response.getBody().getErrors().get("items")).isEqualTo("must not be empty");
    }

    @Test
    void handleMethodArgumentNotValidException_multipleFieldErrors_allPresentInMap()
            throws Exception {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new CreateRequestDto(), "createRequestDto");
        bindingResult.rejectValue("items", "NotNull", "must not be null");
        bindingResult.rejectValue("items", "Size", "must have at least one item");

        // Spring collapses multiple errors for the same field to the last one in the map;
        // assert the key exists regardless of which message wins.
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new MethodParameter(
                        GlobalExceptionHandlerTest.class.getDeclaredMethod("setUp"), -1),
                bindingResult);

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentNotValidException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody().getErrors()).isNotNull();
        assertThat(response.getBody().getErrors()).containsKey("items");
    }

    @Test
    void handleMethodArgumentNotValidException_viaMockMvc_returns400WithErrorsNode()
            throws Exception {
        // POST with empty items array triggers @Valid → MethodArgumentNotValidException
        String invalidBody = """
                {"items": []}
                """;

        mockMvc.perform(post("/api/requests")
                        .header("X-User-Name", "alice")
                        .header("X-User-Role", "STUDENT")
                        .contentType("application/json")
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").exists());
    }

    // ─── handleFeignException ─────────────────────────────────────────────────

    @Test
    void handleFeignException_serviceUnavailable_returnsStatusFromException() {
        FeignException.ServiceUnavailable ex =
                new FeignException.ServiceUnavailable(
                        "inventory-service unavailable",
                        dummyFeignRequest(),
                        null,
                        Collections.emptyMap());

        ResponseEntity<ErrorResponse> response =
                handler.handleFeignException(ex);

        // Handler uses: HttpStatus.valueOf(ex.status() > 0 ? ex.status() : 503)
        // ServiceUnavailable.status() == 503
        assertThat(response.getStatusCodeValue()).isEqualTo(503);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(503);
        assertThat(response.getBody().getMessage()).contains("inventory service");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrors()).isNull();
    }

    @Test
    void handleFeignException_badRequest_returns400() {
        FeignException.BadRequest ex =
                new FeignException.BadRequest(
                        "Insufficient stock",
                        dummyFeignRequest(),
                        null,
                        Collections.emptyMap());

        ResponseEntity<ErrorResponse> response =
                handler.handleFeignException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("inventory service");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleFeignException_notFound_returns404() {
        FeignException.NotFound ex =
                new FeignException.NotFound(
                        "Item not found",
                        dummyFeignRequest(),
                        null,
                        Collections.emptyMap());

        ResponseEntity<ErrorResponse> response =
                handler.handleFeignException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleFeignException_negativeStatus_defaults503() {
        // When ex.status() <= 0 the handler substitutes 503
        FeignException ex = new FeignException.FeignClientException(
                -1,
                "connection refused",
                dummyFeignRequest(),
                null,
                Collections.emptyMap());

        ResponseEntity<ErrorResponse> response =
                handler.handleFeignException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(503);
        assertThat(response.getBody().getStatus()).isEqualTo(503);
    }

    // ─── handleDecodeException ────────────────────────────────────────────────

    @Test
    void handleDecodeException_returns502() {
        DecodeException ex = new DecodeException(
                502,
                "Failed to decode inventory response",
                dummyFeignRequest());

        ResponseEntity<ErrorResponse> response =
                handler.handleDecodeException(ex);

        // Handler always returns BAD_GATEWAY (502)
        assertThat(response.getStatusCodeValue()).isEqualTo(502);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(502);
        assertThat(response.getBody().getMessage()).contains("inventory service");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrors()).isNull();
    }

    @Test
    void handleDecodeException_messageContainsOriginalCause() {
        DecodeException ex = new DecodeException(
                500,
                "Malformed JSON",
                dummyFeignRequest());

        ResponseEntity<ErrorResponse> response =
                handler.handleDecodeException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(502);
        // Handler prefixes with "Failed to decode response from inventory service: "
        assertThat(response.getBody().getMessage()).startsWith("Failed to decode response from inventory service:");
    }

    // ─── handleIllegalArgumentException ──────────────────────────────────────

    @Test
    void handleIllegalArgumentException_returns400WithExactMessage() {
        IllegalArgumentException ex =
                new IllegalArgumentException(
                        "Invalid sort field: xyz. Valid values are: date, status");

        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgumentException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage())
                .isEqualTo("Invalid sort field: xyz. Valid values are: date, status");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrors()).isNull();
    }

    @Test
    void handleIllegalArgumentException_invalidStatus_messagePassedThrough() {
        IllegalArgumentException ex =
                new IllegalArgumentException(
                        "Invalid request status: BOGUS. Valid values are: PENDING, APPROVED, REJECTED, FULFILLED");

        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgumentException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("BOGUS");
    }

    @Test
    void handleIllegalArgumentException_accessDenied_messagePassedThrough() {
        IllegalArgumentException ex =
                new IllegalArgumentException(
                        "Access denied. Required role: ADMIN, but got: STUDENT");

        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgumentException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("ADMIN");
    }

    @Test
    void handleIllegalArgumentException_viaMockMvc_returns400() throws Exception {
        // Non-ADMIN role on GET /api/requests triggers validateRole → IllegalArgumentException
        mockMvc.perform(get("/api/requests")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─── handleIllegalStateException ─────────────────────────────────────────
    // NOTE: handler returns 409 CONFLICT, not 400 BAD REQUEST.

    @Test
    void handleIllegalStateException_returns409WithExactMessage() {
        IllegalStateException ex =
                new IllegalStateException(
                        "Request can only be approved when in PENDING status. Current status: APPROVED");

        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalStateException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getMessage())
                .isEqualTo("Request can only be approved when in PENDING status. Current status: APPROVED");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrors()).isNull();
    }

    @Test
    void handleIllegalStateException_fulfillNotApproved_returns409() {
        IllegalStateException ex =
                new IllegalStateException(
                        "Request can only be fulfilled when in APPROVED status. Current status: PENDING");

        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalStateException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).contains("APPROVED");
    }

    @Test
    void handleIllegalStateException_rejectNotPending_returns409() {
        IllegalStateException ex =
                new IllegalStateException(
                        "Request can only be rejected when in PENDING status. Current status: FULFILLED");

        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalStateException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).contains("FULFILLED");
    }

    @Test
    void handleIllegalStateException_viaMockMvc_returns409() throws Exception {
        when(requestService.fulfillRequest(1L, "admin"))
                .thenThrow(new IllegalStateException(
                        "Request can only be fulfilled when in APPROVED status. Current status: PENDING"));

        mockMvc.perform(put("/api/requests/1/fulfill")
        .header("X-User-Name", "admin")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─── handleGenericException ───────────────────────────────────────────────

    @Test
    void handleGenericException_returns500WithFixedMessage() {
        Exception ex = new Exception("Some internal detail that must not leak");

        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage())
                .isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrors()).isNull();
    }

    @Test
    void handleGenericException_nullPointerException_messageIsAlwaysFixed() {
        NullPointerException ex = new NullPointerException("null ref at service layer");

        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        // Internal cause must NOT appear in public response
        assertThat(response.getBody().getMessage())
                .isEqualTo("An unexpected error occurred");
    }

    @Test
    void handleGenericException_runtimeException_messageIsAlwaysFixed() {
        RuntimeException ex = new RuntimeException("DB connection pool exhausted");

        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(ex);

        assertThat(response.getBody().getMessage())
                .isEqualTo("An unexpected error occurred");
    }

    @Test
    void handleGenericException_viaMockMvc_returns500() throws Exception {
        when(requestService.approveRequest(1L, "admin"))
                .thenThrow(new RuntimeException("Unexpected downstream failure"));

        mockMvc.perform(put("/api/requests/1/approve")
                        .header("X-User-Name", "admin")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─── ErrorResponse structural contract ───────────────────────────────────

    @Test
    void errorResponse_allArgsConstructor_setsAllFourFields() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.util.Map<String, String> errors = java.util.Map.of("items", "must not be empty");

        ErrorResponse response = new ErrorResponse(400, "Validation failed", now, errors);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.getErrors()).containsEntry("items", "must not be empty");
    }

    @Test
    void errorResponse_allArgsConstructor_nullErrors_isPermitted() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        ErrorResponse response = new ErrorResponse(404, "Not found", now, null);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("Not found");
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void errorResponse_builder_roundTrip() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        ErrorResponse response = ErrorResponse.builder()
                .status(503)
                .message("Service unavailable")
                .timestamp(now)
                .errors(null)
                .build();

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getMessage()).isEqualTo("Service unavailable");
        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void errorResponse_setters_overwriteConstructorValues() {
        java.time.LocalDateTime original = java.time.LocalDateTime.now().minusHours(1);
        java.time.LocalDateTime updated  = java.time.LocalDateTime.now();

        ErrorResponse response = new ErrorResponse(400, "old message", original, null);
        response.setStatus(404);
        response.setMessage("new message");
        response.setTimestamp(updated);
        response.setErrors(java.util.Map.of("field", "error"));

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("new message");
        assertThat(response.getTimestamp()).isEqualTo(updated);
        assertThat(response.getErrors()).containsEntry("field", "error");
    }
}
