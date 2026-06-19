package com.stationery.request.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InventoryItemResponseTest {

    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void noArgConstructor_defaultValues() {
        InventoryItemResponse response = new InventoryItemResponse();
        assertNull(response.getId());
        assertNull(response.getName());
        assertNull(response.getCategory());
        assertNull(response.getUnit());
        assertNull(response.getAvailableQuantity());
        assertNull(response.getMinimumQuantity());
        assertNull(response.getDescription());
        assertFalse(response.isLowStock());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
    }

    @Test
    void allArgConstructor_setsAllFields() {
        InventoryItemResponse response = new InventoryItemResponse(
                1L, "Pen", "Writing", "pieces", 100, 10,
                "Blue ballpoint pen", true, now, now);

        assertEquals(1L, response.getId());
        assertEquals("Pen", response.getName());
        assertEquals("Writing", response.getCategory());
        assertEquals("pieces", response.getUnit());
        assertEquals(100, response.getAvailableQuantity());
        assertEquals(10, response.getMinimumQuantity());
        assertEquals("Blue ballpoint pen", response.getDescription());
        assertTrue(response.isLowStock());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    @Test
    void setters_workCorrectly() {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(2L);
        response.setName("Notebook");
        response.setCategory("Paper");
        response.setUnit("books");
        response.setAvailableQuantity(50);
        response.setMinimumQuantity(5);
        response.setDescription("A4 notebook");
        response.setLowStock(false);
        response.setCreatedAt(now);
        response.setUpdatedAt(now);

        assertEquals(2L, response.getId());
        assertEquals("Notebook", response.getName());
        assertEquals("Paper", response.getCategory());
        assertEquals("books", response.getUnit());
        assertEquals(50, response.getAvailableQuantity());
        assertEquals(5, response.getMinimumQuantity());
        assertEquals("A4 notebook", response.getDescription());
        assertFalse(response.isLowStock());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    @Test
    void builder_setsAllFields() {
        InventoryItemResponse response = InventoryItemResponse.builder()
                .id(3L)
                .name("Eraser")
                .category("Correction")
                .unit("pieces")
                .availableQuantity(200)
                .minimumQuantity(20)
                .description("White eraser")
                .lowStock(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(3L, response.getId());
        assertEquals("Eraser", response.getName());
        assertEquals("Correction", response.getCategory());
        assertEquals("pieces", response.getUnit());
        assertEquals(200, response.getAvailableQuantity());
        assertEquals(20, response.getMinimumQuantity());
        assertEquals("White eraser", response.getDescription());
        assertFalse(response.isLowStock());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    @Test
    void builder_lowStock_true() {
        InventoryItemResponse response = InventoryItemResponse.builder()
                .id(4L)
                .name("Stapler")
                .availableQuantity(2)
                .minimumQuantity(10)
                .lowStock(true)
                .build();

        assertTrue(response.isLowStock());
        assertEquals(4L, response.getId());
    }
}