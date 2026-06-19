package com.stationery.inventory.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StationeryItemRequestTest {

    @Test
    void defaultConstructor_fieldsAreNull() {
        StationeryItemRequest request = new StationeryItemRequest();

        assertThat(request.getName()).isNull();
        assertThat(request.getCategory()).isNull();
        assertThat(request.getUnit()).isNull();
        assertThat(request.getAvailableQuantity()).isNull();
        assertThat(request.getMinimumQuantity()).isNull();
        assertThat(request.getDescription()).isNull();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        StationeryItemRequest request = new StationeryItemRequest(
                "Blue Pen", "PEN", "piece", 100, 10, "A blue pen"
        );

        assertThat(request.getName()).isEqualTo("Blue Pen");
        assertThat(request.getCategory()).isEqualTo("PEN");
        assertThat(request.getUnit()).isEqualTo("piece");
        assertThat(request.getAvailableQuantity()).isEqualTo(100);
        assertThat(request.getMinimumQuantity()).isEqualTo(10);
        assertThat(request.getDescription()).isEqualTo("A blue pen");
    }

    @Test
    void builder_setsAllFields() {
        StationeryItemRequest request = StationeryItemRequest.builder()
                .name("Blue Pen")
                .category("PEN")
                .unit("piece")
                .availableQuantity(100)
                .minimumQuantity(10)
                .description("A blue pen")
                .build();

        assertThat(request.getName()).isEqualTo("Blue Pen");
        assertThat(request.getCategory()).isEqualTo("PEN");
        assertThat(request.getUnit()).isEqualTo("piece");
        assertThat(request.getAvailableQuantity()).isEqualTo(100);
        assertThat(request.getMinimumQuantity()).isEqualTo(10);
        assertThat(request.getDescription()).isEqualTo("A blue pen");
    }

    @Test
    void setters_updateFields() {
        StationeryItemRequest request = new StationeryItemRequest();

        request.setName("Red Pen");
        request.setCategory("PEN");
        request.setUnit("box");
        request.setAvailableQuantity(50);
        request.setMinimumQuantity(5);
        request.setDescription("Red ink pen");

        assertThat(request.getName()).isEqualTo("Red Pen");
        assertThat(request.getCategory()).isEqualTo("PEN");
        assertThat(request.getUnit()).isEqualTo("box");
        assertThat(request.getAvailableQuantity()).isEqualTo(50);
        assertThat(request.getMinimumQuantity()).isEqualTo(5);
        assertThat(request.getDescription()).isEqualTo("Red ink pen");
    }

    @Test
    void builder_withNullDescription_buildsSuccessfully() {
        StationeryItemRequest request = StationeryItemRequest.builder()
                .name("Pencil")
                .category("PENCIL")
                .unit("piece")
                .availableQuantity(200)
                .minimumQuantity(20)
                .build();

        assertThat(request.getName()).isEqualTo("Pencil");
        assertThat(request.getDescription()).isNull();
    }

    @Test
    void builder_withZeroQuantities_buildsSuccessfully() {
        StationeryItemRequest request = StationeryItemRequest.builder()
                .name("Paper")
                .category("PAPER")
                .unit("ream")
                .availableQuantity(0)
                .minimumQuantity(0)
                .build();

        assertThat(request.getAvailableQuantity()).isEqualTo(0);
        assertThat(request.getMinimumQuantity()).isEqualTo(0);
    }
}

class StationeryItemResponseTest {

    @Test
    void defaultConstructor_fieldsAreDefault() {
        StationeryItemResponse response = new StationeryItemResponse();

        assertThat(response.getId()).isNull();
        assertThat(response.getName()).isNull();
        assertThat(response.isLowStock()).isFalse();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        StationeryItemResponse response = new StationeryItemResponse(
                1L, "Blue Pen", "PEN", "piece", 100, 10, "A blue pen", false, now, now
        );

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Blue Pen");
        assertThat(response.getCategory()).isEqualTo("PEN");
        assertThat(response.getUnit()).isEqualTo("piece");
        assertThat(response.getAvailableQuantity()).isEqualTo(100);
        assertThat(response.getMinimumQuantity()).isEqualTo(10);
        assertThat(response.getDescription()).isEqualTo("A blue pen");
        assertThat(response.isLowStock()).isFalse();
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void builder_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        StationeryItemResponse response = StationeryItemResponse.builder()
                .id(2L)
                .name("Eraser")
                .category("ERASER")
                .unit("piece")
                .availableQuantity(5)
                .minimumQuantity(10)
                .description("White eraser")
                .lowStock(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Eraser");
        assertThat(response.getCategory()).isEqualTo("ERASER");
        assertThat(response.getUnit()).isEqualTo("piece");
        assertThat(response.getAvailableQuantity()).isEqualTo(5);
        assertThat(response.getMinimumQuantity()).isEqualTo(10);
        assertThat(response.getDescription()).isEqualTo("White eraser");
        assertThat(response.isLowStock()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void setters_updateAllFields() {
        LocalDateTime now = LocalDateTime.now();
        StationeryItemResponse response = new StationeryItemResponse();

        response.setId(3L);
        response.setName("Notebook");
        response.setCategory("NOTEBOOK");
        response.setUnit("piece");
        response.setAvailableQuantity(30);
        response.setMinimumQuantity(5);
        response.setDescription("A5 notebook");
        response.setLowStock(false);
        response.setCreatedAt(now);
        response.setUpdatedAt(now);

        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getName()).isEqualTo("Notebook");
        assertThat(response.getCategory()).isEqualTo("NOTEBOOK");
        assertThat(response.getUnit()).isEqualTo("piece");
        assertThat(response.getAvailableQuantity()).isEqualTo(30);
        assertThat(response.getMinimumQuantity()).isEqualTo(5);
        assertThat(response.getDescription()).isEqualTo("A5 notebook");
        assertThat(response.isLowStock()).isFalse();
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void lowStock_flagSetToTrue() {
        StationeryItemResponse response = StationeryItemResponse.builder()
                .id(1L)
                .name("Marker")
                .lowStock(true)
                .build();

        assertThat(response.isLowStock()).isTrue();
    }

    @Test
    void builder_withNullDescription_buildsSuccessfully() {
        StationeryItemResponse response = StationeryItemResponse.builder()
                .id(1L)
                .name("Folder")
                .category("FOLDER")
                .unit("piece")
                .availableQuantity(20)
                .minimumQuantity(5)
                .build();

        assertThat(response.getDescription()).isNull();
    }
}