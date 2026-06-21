package com.stationery.request.service;

import com.stationery.request.client.InventoryClient;
import com.stationery.request.dto.CreateRequestDto;
import com.stationery.request.dto.RequestItemDto;
import com.stationery.request.dto.RequestResponse;
import com.stationery.request.exception.InsufficientStockException;
import com.stationery.request.exception.ResourceNotFoundException;
import com.stationery.request.model.RequestItem;
import com.stationery.request.model.RequestStatus;
import com.stationery.request.model.StationeryRequest;
import com.stationery.request.repository.RequestRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
private AuditService auditService;

    @InjectMocks
    private RequestService requestService;

    private StationeryRequest pendingRequest;
    private StationeryRequest approvedRequest;
    private RequestItem requestItem;

    @BeforeEach
    void setUp() {
        requestItem = RequestItem.builder()
                .id(1L).itemId(10L).itemName("Pen").quantity(5).build();

        pendingRequest = buildRequest(1L, "uuid-pending", "student1", RequestStatus.PENDING, List.of(requestItem));
        approvedRequest = buildRequest(2L, "uuid-approved", "student1", RequestStatus.APPROVED, List.of(requestItem));
    }

    private StationeryRequest buildRequest(Long id, String requestId, String student,
                                            RequestStatus status, List<RequestItem> items) {
        StationeryRequest r = new StationeryRequest();
        r.setId(id);
        r.setRequestId(requestId);
        r.setStudentUsername(student);
        r.setStatus(status);
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        // wire up items
        List<RequestItem> mutableItems = new ArrayList<>(items);
        r.setItems(mutableItems);
        for (RequestItem item : mutableItems) item.setRequest(r);
        return r;
    }

    // ===== createRequest =====

    @Test
    void createRequest_success() {
        RequestItemDto itemDto = RequestItemDto.builder()
                .itemId(10L).itemName("Pen").quantity(5).build();
        CreateRequestDto createDto = new CreateRequestDto();
        createDto.setItems(List.of(itemDto));

        when(requestRepository.save(any(StationeryRequest.class))).thenAnswer(inv -> {
            StationeryRequest r = inv.getArgument(0);
            r.setId(1L);
            r.setRequestId("uuid-new");
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(LocalDateTime.now());
            return r;
        });

        RequestResponse response = requestService.createRequest("student1", createDto);

        assertNotNull(response);
        assertEquals("student1", response.getStudentUsername());
        assertEquals("PENDING", response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals("Pen", response.getItems().get(0).getItemName());
    }

    // ===== getRequestById =====

    @Test
    void getRequestById_found() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));
        RequestResponse response = requestService.getRequestById(1L);
        assertEquals(1L, response.getId());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void getRequestById_notFound_throwsResourceNotFoundException() {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> requestService.getRequestById(99L));
    }

    // ===== getRequestByRequestId =====

    @Test
    void getRequestByRequestId_found() {
        when(requestRepository.findByRequestId("uuid-pending")).thenReturn(Optional.of(pendingRequest));
        RequestResponse response = requestService.getRequestByRequestId("uuid-pending");
        assertEquals("uuid-pending", response.getRequestId());
    }

    @Test
    void getRequestByRequestId_notFound_throwsResourceNotFoundException() {
        when(requestRepository.findByRequestId("bad-id")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> requestService.getRequestByRequestId("bad-id"));
    }

    // ===== getRequestsByStudent =====

    @Test
    void getRequestsByStudent_returnsList() {
        when(requestRepository.findByStudentUsername("student1")).thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getRequestsByStudent("student1");
        assertEquals(1, responses.size());
        assertEquals("student1", responses.get(0).getStudentUsername());
    }

    // ===== getRequestsByStudentAndStatus =====

    @Test
    void getRequestsByStudentAndStatus_validStatus() {
        when(requestRepository.findByStudentUsernameAndStatus("student1", RequestStatus.PENDING))
                .thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getRequestsByStudentAndStatus("student1", "PENDING");
        assertEquals(1, responses.size());
    }

    @Test
    void getRequestsByStudentAndStatus_invalidStatus_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> requestService.getRequestsByStudentAndStatus("student1", "INVALID"));
    }

    // ===== getAllRequests =====

    @Test
    void getAllRequests_returnsList() {
        when(requestRepository.findAll()).thenReturn(List.of(pendingRequest, approvedRequest));
        List<RequestResponse> responses = requestService.getAllRequests();
        assertEquals(2, responses.size());
    }

    // ===== getAllRequestsByStatus =====

    @Test
    void getAllRequestsByStatus_validStatus() {
        when(requestRepository.findByStatus(RequestStatus.PENDING)).thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getAllRequestsByStatus("PENDING");
        assertEquals(1, responses.size());
        assertEquals("PENDING", responses.get(0).getStatus());
    }

    @Test
    void getAllRequestsByStatus_invalidStatus_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> requestService.getAllRequestsByStatus("UNKNOWN"));
    }

    // ===== getAllRequestsSorted =====

    @Test
    void getAllRequestsSorted_byDateDesc() {
        when(requestRepository.findAllOrderByDateDesc()).thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getAllRequestsSorted("date", "desc");
        assertEquals(1, responses.size());
    }

    @Test
    void getAllRequestsSorted_byDateAsc() {
        when(requestRepository.findAllOrderByDateAsc()).thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getAllRequestsSorted("date", "asc");
        assertEquals(1, responses.size());
    }

    @Test
    void getAllRequestsSorted_byStatus() {
        when(requestRepository.findAllOrderByStatusAsc()).thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getAllRequestsSorted("status", "asc");
        assertEquals(1, responses.size());
    }

    @Test
    void getAllRequestsSorted_invalidSortField_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> requestService.getAllRequestsSorted("invalid", "asc"));
    }

    // ===== getRequestsByStudentSorted =====

    @Test
    void getRequestsByStudentSorted_byDateDesc() {
        when(requestRepository.findByStudentUsernameOrderByDateDesc("student1"))
                .thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getRequestsByStudentSorted("student1", "date", "desc");
        assertEquals(1, responses.size());
    }

    @Test
    void getRequestsByStudentSorted_byDateAsc() {
        when(requestRepository.findByStudentUsernameOrderByDateAsc("student1"))
                .thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getRequestsByStudentSorted("student1", "date", "asc");
        assertEquals(1, responses.size());
    }

    @Test
    void getRequestsByStudentSorted_byStatus() {
        when(requestRepository.findByStudentUsernameOrderByStatusAsc("student1"))
                .thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getRequestsByStudentSorted("student1", "status", "asc");
        assertEquals(1, responses.size());
    }

    @Test
    void getRequestsByStudentSorted_invalidSortField_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> requestService.getRequestsByStudentSorted("student1", "bad", "asc"));
    }

    // ===== getRequestsByStatusSorted =====

    @Test
    void getRequestsByStatusSorted_byDateDesc() {
        when(requestRepository.findByStatusOrderByDateDesc(RequestStatus.PENDING))
                .thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getRequestsByStatusSorted("PENDING", "date", "desc");
        assertEquals(1, responses.size());
    }

    @Test
    void getRequestsByStatusSorted_byDateAsc() {
        when(requestRepository.findByStatusOrderByDateAsc(RequestStatus.PENDING))
                .thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getRequestsByStatusSorted("PENDING", "date", "asc");
        assertEquals(1, responses.size());
    }

    @Test
    void getRequestsByStatusSorted_byStatus() {
        when(requestRepository.findByStatus(RequestStatus.PENDING))
                .thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getRequestsByStatusSorted("PENDING", "status", "asc");
        assertEquals(1, responses.size());
    }

    @Test
    void getRequestsByStatusSorted_invalidSortField_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> requestService.getRequestsByStatusSorted("PENDING", "bad", "asc"));
    }

    // ===== getRequestsByStudentAndStatusSorted =====

    @Test
    void getRequestsByStudentAndStatusSorted_byDateDesc() {
        when(requestRepository.findByStudentUsernameAndStatusOrderByDateDesc("student1", RequestStatus.PENDING))
                .thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getRequestsByStudentAndStatusSorted(
                "student1", "PENDING", "date", "desc");
        assertEquals(1, responses.size());
    }

    @Test
    void getRequestsByStudentAndStatusSorted_byDateAsc() {
        when(requestRepository.findByStudentUsernameAndStatusOrderByDateAsc("student1", RequestStatus.PENDING))
                .thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getRequestsByStudentAndStatusSorted(
                "student1", "PENDING", "date", "asc");
        assertEquals(1, responses.size());
    }

    @Test
    void getRequestsByStudentAndStatusSorted_byStatus() {
        when(requestRepository.findByStudentUsernameAndStatus("student1", RequestStatus.PENDING))
                .thenReturn(List.of(pendingRequest));
        List<RequestResponse> responses = requestService.getRequestsByStudentAndStatusSorted(
                "student1", "PENDING", "status", "asc");
        assertEquals(1, responses.size());
    }

    @Test
    void getRequestsByStudentAndStatusSorted_invalidSortField_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> requestService.getRequestsByStudentAndStatusSorted(
                        "student1", "PENDING", "bad", "asc"));
    }

    @Test
    void fulfillRequest_notApproved_throws() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));
        assertThrows(IllegalStateException.class, () -> requestService.fulfillRequest(1L, "admin"));
    }

    @Test
    void fulfillRequest_notFound_throws() {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> requestService.fulfillRequest(99L, "admin"));
    }
}