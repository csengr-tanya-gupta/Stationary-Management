package com.stationery.request.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApproveRejectDtoTest {

    @Test
    void noArgConstructor_defaultsToNull() {
        ApproveRejectDto dto = new ApproveRejectDto();
        assertNull(dto.getRejectionReason());
    }

    @Test
    void allArgConstructor_setsReason() {
        ApproveRejectDto dto = new ApproveRejectDto("Out of stock");
        assertEquals("Out of stock", dto.getRejectionReason());
    }

    @Test
    void setter_updatesReason() {
        ApproveRejectDto dto = new ApproveRejectDto();
        dto.setRejectionReason("Budget exceeded");
        assertEquals("Budget exceeded", dto.getRejectionReason());
    }

    @Test
    void builder_setsReason() {
        ApproveRejectDto dto = ApproveRejectDto.builder()
                .rejectionReason("Duplicate request")
                .build();
        assertEquals("Duplicate request", dto.getRejectionReason());
    }

    @Test
    void builder_noReason_buildsSuccessfully() {
        ApproveRejectDto dto = ApproveRejectDto.builder().build();
        assertNull(dto.getRejectionReason());
    }

    @Test
    void setter_toNull_works() {
        ApproveRejectDto dto = new ApproveRejectDto("reason");
        dto.setRejectionReason(null);
        assertNull(dto.getRejectionReason());
    }
}