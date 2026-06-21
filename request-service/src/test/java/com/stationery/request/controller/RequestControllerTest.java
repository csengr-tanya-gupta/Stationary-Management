package com.stationery.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stationery.request.dto.ApproveRejectDto;
import com.stationery.request.dto.CreateRequestDto;
import com.stationery.request.dto.RequestItemDto;
import com.stationery.request.dto.RequestResponse;
import com.stationery.request.service.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RequestControllerTest {

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RequestResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(requestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        RequestItemDto itemDto = RequestItemDto.builder()
                .itemId(1L)
                .itemName("Pen")
                .quantity(2)
                .build();

        sampleResponse = RequestResponse.builder()
                .id(1L)
                .requestId("uuid-1234")
                .studentUsername("student1")
                .items(List.of(itemDto))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ===== POST /api/requests =====

    @Test
    void createRequest_success() throws Exception {
        RequestItemDto itemDto = RequestItemDto.builder()
                .itemId(1L).itemName("Pen").quantity(2).build();
        CreateRequestDto createDto = new CreateRequestDto();
        createDto.setItems(List.of(itemDto));

        when(requestService.createRequest(eq("student1"), any(CreateRequestDto.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/requests")
                        .header("X-User-Name", "student1")
                        .header("X-User-Role", "STUDENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value("uuid-1234"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    

    // ===== GET /api/requests/my =====

    @Test
    void getMyRequests_noFilter_noSort() throws Exception {
        when(requestService.getRequestsByStudent("student1"))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/requests/my")
                        .header("X-User-Name", "student1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentUsername").value("student1"));
    }

    @Test
    void getMyRequests_withStatusFilter() throws Exception {
        when(requestService.getRequestsByStudentAndStatus("student1", "PENDING"))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/requests/my")
                        .header("X-User-Name", "student1")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getMyRequests_withSortBy_noStatus() throws Exception {
        when(requestService.getRequestsByStudentSorted("student1", "date", "asc"))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/requests/my")
                        .header("X-User-Name", "student1")
                        .param("sortBy", "date")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyRequests_withSortByAndStatus() throws Exception {
        when(requestService.getRequestsByStudentAndStatusSorted("student1", "PENDING", "date", "desc"))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/requests/my")
                        .header("X-User-Name", "student1")
                        .param("status", "PENDING")
                        .param("sortBy", "date")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk());
    }

    // ===== GET /api/requests/{id} =====

    @Test
    void getRequestById_found() throws Exception {
        when(requestService.getRequestById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/requests/1")
                        .header("X-User-Name", "student1")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getRequestById_adminCanAccessAnyRequest() throws Exception {
        when(requestService.getRequestById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/requests/1")
                        .header("X-User-Name", "admin1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_otherStudentIsForbidden() throws Exception {
        when(requestService.getRequestById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/requests/1")
                        .header("X-User-Name", "student2")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    // ===== GET /api/requests/track/{requestId} =====

    @Test
    void getRequestByRequestId_found() throws Exception {
        when(requestService.getRequestByRequestId("uuid-1234")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/requests/track/uuid-1234")
                        .header("X-User-Name", "student1")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("uuid-1234"));
    }

    @Test
    void getRequestByRequestId_adminCanAccessAnyRequest() throws Exception {
        when(requestService.getRequestByRequestId("uuid-1234")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/requests/track/uuid-1234")
                        .header("X-User-Name", "admin1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestByRequestId_otherStudentIsForbidden() throws Exception {
        when(requestService.getRequestByRequestId("uuid-1234")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/requests/track/uuid-1234")
                        .header("X-User-Name", "student2")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    // ===== GET /api/requests (Admin) =====

    @Test
    void getAllRequests_adminRole_noFilter_noSort() throws Exception {
        when(requestService.getAllRequests()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/requests")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentUsername").value("student1"));
    }

    

    @Test
    void getAllRequests_withStatusFilter() throws Exception {
        when(requestService.getAllRequestsByStatus("PENDING")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/requests")
                        .header("X-User-Role", "ADMIN")
                        .param("status", "PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_withSortBy_noStatus() throws Exception {
        when(requestService.getAllRequestsSorted("date", "desc")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/requests")
                        .header("X-User-Role", "ADMIN")
                        .param("sortBy", "date")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_withSortByAndStatus() throws Exception {
        when(requestService.getRequestsByStatusSorted("PENDING", "date", "asc"))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/requests")
                        .header("X-User-Role", "ADMIN")
                        .param("status", "PENDING")
                        .param("sortBy", "date")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk());
    }

    // ===== PUT /api/requests/{id}/approve =====

    @Test
    void approveRequest_success() throws Exception {
        RequestResponse approvedResponse = RequestResponse.builder()
                .id(1L).requestId("uuid-1234").studentUsername("student1")
                .items(Collections.emptyList()).status("APPROVED")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(requestService.approveRequest(1L, "admin1")).thenReturn(approvedResponse);

        mockMvc.perform(put("/api/requests/1/approve")
                        .header("X-User-Name", "admin1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    

    // ===== PUT /api/requests/{id}/reject =====

    @Test
    void rejectRequest_withReason() throws Exception {
        RequestResponse rejectedResponse = RequestResponse.builder()
                .id(1L).requestId("uuid-1234").studentUsername("student1")
                .items(Collections.emptyList()).status("REJECTED")
                .rejectionReason("Out of stock")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        ApproveRejectDto dto = new ApproveRejectDto("Out of stock");
        when(requestService.rejectRequest(1L, "admin1", "Out of stock")).thenReturn(rejectedResponse);

        mockMvc.perform(put("/api/requests/1/reject")
                        .header("X-User-Name", "admin1")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Out of stock"));
    }

    @Test
    void rejectRequest_noBody_usesNullReason() throws Exception {
        RequestResponse rejectedResponse = RequestResponse.builder()
                .id(1L).requestId("uuid-1234").studentUsername("student1")
                .items(Collections.emptyList()).status("REJECTED")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(requestService.rejectRequest(1L, "admin1", null)).thenReturn(rejectedResponse);

        mockMvc.perform(put("/api/requests/1/reject")
                        .header("X-User-Name", "admin1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    
    // ===== PUT /api/requests/{id}/fulfill =====

    @Test
    void fulfillRequest_success() throws Exception {
        RequestResponse fulfilledResponse = RequestResponse.builder()
                .id(1L).requestId("uuid-1234").studentUsername("student1")
                .items(Collections.emptyList()).status("FULFILLED")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(requestService.fulfillRequest(1L, "admin")).thenReturn(fulfilledResponse);

        mockMvc.perform(put("/api/requests/1/fulfill")
        .header("X-User-Name", "admin")
        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FULFILLED"));
    }

}
