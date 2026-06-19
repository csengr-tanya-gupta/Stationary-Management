package com.stationery.inventory.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StationeryItemTest {

    @Test
    void defaultConstructor_fieldsAreNull() {
        StationeryItem item = new StationeryItem();

        assertThat(item.getId()).isNull();
        assertThat(item.getName()).isNull();
        assertThat(item.getCategory()).isNull();
        assertThat(item.getUnit()).isNull();
        assertThat(item.getAvailableQuantity()).isNull();
        assertThat(item.getMinimumQuantity()).isNull();
        assertThat(item.getDescription()).isNull();
        assertThat(item.getCreatedAt()).isNull();
        assertThat(item.getUpdatedAt()).isNull();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        StationeryItem item = new StationeryItem(
                1L, "Blue Pen", "PEN", "piece", 100, 10, "A blue pen", now, now
        );

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Blue Pen");
        assertThat(item.getCategory()).isEqualTo("PEN");
        assertThat(item.getUnit()).isEqualTo("piece");
        assertThat(item.getAvailableQuantity()).isEqualTo(100);
        assertThat(item.getMinimumQuantity()).isEqualTo(10);
        assertThat(item.getDescription()).isEqualTo("A blue pen");
        assertThat(item.getCreatedAt()).isEqualTo(now);
        assertThat(item.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void builder_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        StationeryItem item = StationeryItem.builder()
                .id(2L)
                .name("Eraser")
                .category("ERASER")
                .unit("piece")
                .availableQuantity(50)
                .minimumQuantity(5)
                .description("White eraser")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(item.getId()).isEqualTo(2L);
        assertThat(item.getName()).isEqualTo("Eraser");
        assertThat(item.getCategory()).isEqualTo("ERASER");
        assertThat(item.getUnit()).isEqualTo("piece");
        assertThat(item.getAvailableQuantity()).isEqualTo(50);
        assertThat(item.getMinimumQuantity()).isEqualTo(5);
        assertThat(item.getDescription()).isEqualTo("White eraser");
        assertThat(item.getCreatedAt()).isEqualTo(now);
        assertThat(item.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void setters_updateAllFields() {
        LocalDateTime now = LocalDateTime.now();
        StationeryItem item = new StationeryItem();

        item.setId(3L);
        item.setName("Notebook");
        item.setCategory("NOTEBOOK");
        item.setUnit("piece");
        item.setAvailableQuantity(30);
        item.setMinimumQuantity(3);
        item.setDescription("A5 notebook");
        item.setCreatedAt(now);
        item.setUpdatedAt(now);

        assertThat(item.getId()).isEqualTo(3L);
        assertThat(item.getName()).isEqualTo("Notebook");
        assertThat(item.getCategory()).isEqualTo("NOTEBOOK");
        assertThat(item.getUnit()).isEqualTo("piece");
        assertThat(item.getAvailableQuantity()).isEqualTo(30);
        assertThat(item.getMinimumQuantity()).isEqualTo(3);
        assertThat(item.getDescription()).isEqualTo("A5 notebook");
        assertThat(item.getCreatedAt()).isEqualTo(now);
        assertThat(item.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void builder_withNullDescription_buildsSuccessfully() {
        StationeryItem item = StationeryItem.builder()
                .id(1L)
                .name("Paper")
                .category("PAPER")
                .unit("ream")
                .availableQuantity(500)
                .minimumQuantity(50)
                .build();

        assertThat(item.getName()).isEqualTo("Paper");
        assertThat(item.getDescription()).isNull();
    }

    @Test
    void builder_withZeroQuantity_buildsSuccessfully() {
        StationeryItem item = StationeryItem.builder()
                .id(5L)
                .name("Marker")
                .category("MARKER")
                .unit("piece")
                .availableQuantity(0)
                .minimumQuantity(0)
                .build();

        assertThat(item.getAvailableQuantity()).isEqualTo(0);
        assertThat(item.getMinimumQuantity()).isEqualTo(0);
    }

    @Test
    void setAvailableQuantity_updatesQuantity() {
        StationeryItem item = StationeryItem.builder()
                .id(1L).name("Pen").category("PEN").unit("piece")
                .availableQuantity(100).minimumQuantity(10).build();

        item.setAvailableQuantity(95);

        assertThat(item.getAvailableQuantity()).isEqualTo(95);
    }

    @Test
    void setUpdatedAt_updatesTimestamp() {
        StationeryItem item = new StationeryItem();
        LocalDateTime time = LocalDateTime.of(2024, 1, 15, 10, 30);

        item.setUpdatedAt(time);

        assertThat(item.getUpdatedAt()).isEqualTo(time);
    }
}

class CategoryTest {

    @Test
    void categoryEnum_containsAllExpectedValues() {
        Category[] values = Category.values();

        assertThat(values).contains(
                Category.PAPER,
                Category.PEN,
                Category.PENCIL,
                Category.NOTEBOOK,
                Category.ERASER,
                Category.MARKER,
                Category.FOLDER,
                Category.STAPLER,
                Category.OTHER
        );
    }

    @Test
    void categoryEnum_valueOf_returnsCorrectEnum() {
        assertThat(Category.valueOf("PEN")).isEqualTo(Category.PEN);
        assertThat(Category.valueOf("PAPER")).isEqualTo(Category.PAPER);
        assertThat(Category.valueOf("NOTEBOOK")).isEqualTo(Category.NOTEBOOK);
        assertThat(Category.valueOf("ERASER")).isEqualTo(Category.ERASER);
        assertThat(Category.valueOf("MARKER")).isEqualTo(Category.MARKER);
        assertThat(Category.valueOf("FOLDER")).isEqualTo(Category.FOLDER);
        assertThat(Category.valueOf("STAPLER")).isEqualTo(Category.STAPLER);
        assertThat(Category.valueOf("PENCIL")).isEqualTo(Category.PENCIL);
        assertThat(Category.valueOf("OTHER")).isEqualTo(Category.OTHER);
    }

    @Test
    void categoryEnum_name_returnsCorrectString() {
        assertThat(Category.PEN.name()).isEqualTo("PEN");
        assertThat(Category.PAPER.name()).isEqualTo("PAPER");
        assertThat(Category.OTHER.name()).isEqualTo("OTHER");
    }

    @Test
    void categoryEnum_ordinal_isCorrect() {
        assertThat(Category.PAPER.ordinal()).isEqualTo(0);
        assertThat(Category.PEN.ordinal()).isEqualTo(1);
    }

    @Test
    void categoryEnum_totalCount_isNine() {
        assertThat(Category.values()).hasSize(9);
    }
}