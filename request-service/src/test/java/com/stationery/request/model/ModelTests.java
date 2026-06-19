package com.stationery.request.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestItemTest {

    @Test
    void noArgConstructor_defaultValues() {
        RequestItem item = new RequestItem();
        assertNull(item.getId());
        assertNull(item.getItemId());
        assertNull(item.getItemName());
        assertNull(item.getQuantity());
        assertNull(item.getRequest());
    }

    @Test
    void allArgConstructor_setsAllFields() {
        StationeryRequest request = new StationeryRequest();
        RequestItem item = new RequestItem(1L, 10L, "Pen", 5, request);

        assertEquals(1L, item.getId());
        assertEquals(10L, item.getItemId());
        assertEquals("Pen", item.getItemName());
        assertEquals(5, item.getQuantity());
        assertEquals(request, item.getRequest());
    }

    @Test
    void setters_workCorrectly() {
        StationeryRequest request = new StationeryRequest();
        RequestItem item = new RequestItem();
        item.setId(2L);
        item.setItemId(20L);
        item.setItemName("Notebook");
        item.setQuantity(3);
        item.setRequest(request);

        assertEquals(2L, item.getId());
        assertEquals(20L, item.getItemId());
        assertEquals("Notebook", item.getItemName());
        assertEquals(3, item.getQuantity());
        assertEquals(request, item.getRequest());
    }

    @Test
    void builder_setsAllFields() {
        StationeryRequest request = new StationeryRequest();
        RequestItem item = RequestItem.builder()
                .id(3L)
                .itemId(30L)
                .itemName("Eraser")
                .quantity(10)
                .request(request)
                .build();

        assertEquals(3L, item.getId());
        assertEquals(30L, item.getItemId());
        assertEquals("Eraser", item.getItemName());
        assertEquals(10, item.getQuantity());
        assertEquals(request, item.getRequest());
    }

    @Test
    void builder_withoutRequest_buildsSuccessfully() {
        RequestItem item = RequestItem.builder()
                .itemId(5L)
                .itemName("Stapler")
                .quantity(1)
                .build();

        assertNull(item.getId());
        assertEquals(5L, item.getItemId());
        assertEquals("Stapler", item.getItemName());
        assertEquals(1, item.getQuantity());
        assertNull(item.getRequest());
    }
}

class StationeryRequestTest {

    @Test
    void noArgConstructor_defaultValues() {
        StationeryRequest request = new StationeryRequest();
        assertNull(request.getId());
        assertNull(request.getRequestId());
        assertNull(request.getStudentUsername());
        assertNull(request.getStatus());
        assertNull(request.getRejectionReason());
        assertNull(request.getAdminUsername());
        assertNull(request.getCreatedAt());
        assertNull(request.getUpdatedAt());
        assertNotNull(request.getItems());
        assertTrue(request.getItems().isEmpty());
    }

    @Test
    void allArgConstructor_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        List<RequestItem> items = new ArrayList<>();

        StationeryRequest request = new StationeryRequest(
                1L, "uuid-1", "student1", RequestStatus.PENDING,
                "some reason", "admin1", now, now, items);

        assertEquals(1L, request.getId());
        assertEquals("uuid-1", request.getRequestId());
        assertEquals("student1", request.getStudentUsername());
        assertEquals(RequestStatus.PENDING, request.getStatus());
        assertEquals("some reason", request.getRejectionReason());
        assertEquals("admin1", request.getAdminUsername());
        assertEquals(now, request.getCreatedAt());
        assertEquals(now, request.getUpdatedAt());
        assertNotNull(request.getItems());
    }

    @Test
    void allArgConstructor_nullItems_defaultsToEmptyList() {
        StationeryRequest request = new StationeryRequest(
                1L, "uuid-1", "student1", RequestStatus.PENDING,
                null, null, LocalDateTime.now(), LocalDateTime.now(), null);

        assertNotNull(request.getItems());
        assertTrue(request.getItems().isEmpty());
    }

    @Test
    void setters_workCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        StationeryRequest request = new StationeryRequest();
        request.setId(2L);
        request.setRequestId("uuid-2");
        request.setStudentUsername("student2");
        request.setStatus(RequestStatus.APPROVED);
        request.setRejectionReason("Not needed");
        request.setAdminUsername("admin2");
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        request.setItems(new ArrayList<>());

        assertEquals(2L, request.getId());
        assertEquals("uuid-2", request.getRequestId());
        assertEquals("student2", request.getStudentUsername());
        assertEquals(RequestStatus.APPROVED, request.getStatus());
        assertEquals("Not needed", request.getRejectionReason());
        assertEquals("admin2", request.getAdminUsername());
        assertEquals(now, request.getCreatedAt());
        assertEquals(now, request.getUpdatedAt());
    }

    @Test
    void builder_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        StationeryRequest request = StationeryRequest.builder()
                .id(3L)
                .requestId("uuid-3")
                .studentUsername("student3")
                .status(RequestStatus.REJECTED)
                .rejectionReason("Rejected reason")
                .adminUsername("admin3")
                .createdAt(now)
                .updatedAt(now)
                .items(new ArrayList<>())
                .build();

        assertEquals(3L, request.getId());
        assertEquals("uuid-3", request.getRequestId());
        assertEquals("student3", request.getStudentUsername());
        assertEquals(RequestStatus.REJECTED, request.getStatus());
        assertEquals("Rejected reason", request.getRejectionReason());
        assertEquals("admin3", request.getAdminUsername());
        assertEquals(now, request.getCreatedAt());
        assertEquals(now, request.getUpdatedAt());
    }

    @Test
    void addItem_addsToListAndSetsBackReference() {
        StationeryRequest request = new StationeryRequest();
        request.setItems(new ArrayList<>());
        RequestItem item = new RequestItem();
        item.setItemId(1L);
        item.setItemName("Pen");
        item.setQuantity(2);

        request.addItem(item);

        assertEquals(1, request.getItems().size());
        assertEquals(request, item.getRequest());
    }

    @Test
    void removeItem_removesFromListAndNullsBackReference() {
        StationeryRequest request = new StationeryRequest();
        request.setItems(new ArrayList<>());
        RequestItem item = new RequestItem();
        item.setItemId(1L);
        item.setItemName("Pen");
        item.setQuantity(2);

        request.addItem(item);
        assertEquals(1, request.getItems().size());

        request.removeItem(item);
        assertTrue(request.getItems().isEmpty());
        assertNull(item.getRequest());
    }

    @Test
    void onCreate_setsRequestIdCreatedAtUpdatedAtAndDefaultStatus() {
        StationeryRequest request = new StationeryRequest();
        request.setStudentUsername("student1");
        // Simulate @PrePersist
        invokeOnCreate(request);

        assertNotNull(request.getRequestId());
        assertFalse(request.getRequestId().isEmpty());
        assertNotNull(request.getCreatedAt());
        assertNotNull(request.getUpdatedAt());
        assertEquals(RequestStatus.PENDING, request.getStatus());
    }

    @Test
    void onCreate_doesNotOverwriteExistingRequestId() {
        StationeryRequest request = new StationeryRequest();
        request.setRequestId("existing-uuid");
        request.setStatus(RequestStatus.APPROVED);
        invokeOnCreate(request);

        assertEquals("existing-uuid", request.getRequestId());
        assertEquals(RequestStatus.APPROVED, request.getStatus());
    }

    @Test
    void onUpdate_updatesTimestamp() throws InterruptedException {
        StationeryRequest request = new StationeryRequest();
        invokeOnCreate(request);
        LocalDateTime firstUpdate = request.getUpdatedAt();

        Thread.sleep(5);
        invokeOnUpdate(request);

        assertTrue(request.getUpdatedAt().isAfter(firstUpdate)
                || request.getUpdatedAt().isEqual(firstUpdate));
    }

    @Test
    void requestStatus_allValuesExist() {
        assertEquals(RequestStatus.PENDING, RequestStatus.valueOf("PENDING"));
        assertEquals(RequestStatus.APPROVED, RequestStatus.valueOf("APPROVED"));
        assertEquals(RequestStatus.REJECTED, RequestStatus.valueOf("REJECTED"));
        assertEquals(RequestStatus.FULFILLED, RequestStatus.valueOf("FULFILLED"));
    }

    // ===== Helpers to invoke JPA lifecycle callbacks via reflection =====

    private void invokeOnCreate(StationeryRequest request) {
        try {
            java.lang.reflect.Method method = StationeryRequest.class
                    .getDeclaredMethod("onCreate");
            method.setAccessible(true);
            method.invoke(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke onCreate", e);
        }
    }

    private void invokeOnUpdate(StationeryRequest request) {
        try {
            java.lang.reflect.Method method = StationeryRequest.class
                    .getDeclaredMethod("onUpdate");
            method.setAccessible(true);
            method.invoke(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke onUpdate", e);
        }
    }
}