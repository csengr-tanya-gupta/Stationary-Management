package com.stationery.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stationery.inventory.dto.StationeryItemRequest;
import com.stationery.inventory.dto.StationeryItemResponse;
import com.stationery.inventory.exception.GlobalExceptionHandler;
import com.stationery.inventory.exception.InsufficientStockException;
import com.stationery.inventory.exception.ResourceNotFoundException;
import com.stationery.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private StationeryItemRequest validRequest;
    private StationeryItemResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(inventoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        validRequest = StationeryItemRequest.builder()
                .name("Blue Pen")
                .category("PEN")
                .unit("piece")
                .availableQuantity(100)
                .minimumQuantity(10)
                .description("Blue ballpoint pen")
                .build();

        sampleResponse = StationeryItemResponse.builder()
                .id(1L)
                .name("Blue Pen")
                .category("PEN")
                .unit("piece")
                .availableQuantity(100)
                .minimumQuantity(10)
                .description("Blue ballpoint pen")
                .lowStock(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ===== POST /api/inventory =====

    @Test
    void createItem_asAdmin_returns201() throws Exception {
        when(inventoryService.createItem(any(StationeryItemRequest.class), anyString())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/inventory")
                        .header("X-User-Role", "ADMIN")
                        .header("X-User-Name", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Blue Pen"));
    }

    @Test
    void createItem_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(post("/api/inventory")
                        .header("X-User-Role", "USER")
                        .header("X-User-Name", "john")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(inventoryService);
    }

    @Test
    void createItem_withoutRoleHeader_returns403() throws Exception {
        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(inventoryService);
    }

    @Test
    void createItem_adminRoleCaseInsensitive_returns201() throws Exception {
        when(inventoryService.createItem(any(StationeryItemRequest.class), anyString())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/inventory")
                        .header("X-User-Role", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void createItem_withInvalidRequestBody_returns400() throws Exception {
        // name, category, unit are @NotBlank; quantities are @NotNull — all missing
        StationeryItemRequest invalid = new StationeryItemRequest();

        mockMvc.perform(post("/api/inventory")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ===== GET /api/inventory =====

    // @Test
    // void getAllItems_returnsPagedResults() throws Exception {
    //     Page<StationeryItemResponse> page = new PageImpl<>(List.of(sampleResponse));
    //     when(inventoryService.getAllItems(0, 20, "name")).thenReturn(page);

    //     mockMvc.perform(get("/api/inventory")
    //                     .param("page", "0")
    //                     .param("size", "20")
    //                     .param("sortBy", "name"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.content[0].name").value("Blue Pen"));
    // }

    // @Test
    // void getAllItems_withDefaultParams_returnsOk() throws Exception {
    //     Page<StationeryItemResponse> page = new PageImpl<>(Collections.emptyList());
    //     when(inventoryService.getAllItems(0, 20, "name")).thenReturn(page);

    //     mockMvc.perform(get("/api/inventory"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.content").isEmpty());
    // }

    // ===== GET /api/inventory/{id} =====

    @Test
    void getItemById_existingItem_returns200() throws Exception {
        when(inventoryService.getItemById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Blue Pen"));
    }

    @Test
    void getItemById_nonExistingItem_returns404() throws Exception {
        when(inventoryService.getItemById(99L))
                .thenThrow(new ResourceNotFoundException("Item not found with id: 99"));

        mockMvc.perform(get("/api/inventory/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item not found with id: 99"));
    }

    // ===== GET /api/inventory/category/{category} =====

    // @Test
    // void getItemsByCategory_returnsPagedResults() throws Exception {
    //     Page<StationeryItemResponse> page = new PageImpl<>(List.of(sampleResponse));
    //     when(inventoryService.getItemsByCategory("PEN", 0, 20)).thenReturn(page);

    //     mockMvc.perform(get("/api/inventory/category/PEN")
    //                     .param("page", "0")
    //                     .param("size", "20"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.content[0].category").value("PEN"));
    // }

    // @Test
    // void getItemsByCategory_noResults_returnsEmptyPage() throws Exception {
    //     Page<StationeryItemResponse> page = new PageImpl<>(Collections.emptyList());
    //     when(inventoryService.getItemsByCategory("UNKNOWN", 0, 20)).thenReturn(page);

    //     mockMvc.perform(get("/api/inventory/category/UNKNOWN")
    //                     .param("page", "0")
    //                     .param("size", "20"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.content").isEmpty());
    // }

    // ===== PUT /api/inventory/{id} =====

    @Test
    void updateItem_asAdmin_returns200() throws Exception {
        when(inventoryService.updateItem(eq(1L), any(StationeryItemRequest.class), anyString()))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/inventory/1")
                        .header("X-User-Role", "ADMIN")
                        .header("X-User-Name", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateItem_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(put("/api/inventory/1")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(inventoryService);
    }

    @Test
    void updateItem_withoutRoleHeader_returns403() throws Exception {
        mockMvc.perform(put("/api/inventory/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(inventoryService);
    }

    @Test
    void updateItem_itemNotFound_returns404() throws Exception {
        when(inventoryService.updateItem(eq(99L), any(StationeryItemRequest.class), anyString()))
                .thenThrow(new ResourceNotFoundException("Item not found with id: 99"));

        mockMvc.perform(put("/api/inventory/99")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    // ===== DELETE /api/inventory/{id} =====

    @Test
    void deleteItem_asAdmin_returns204() throws Exception {
        doNothing().when(inventoryService).deleteItem(eq(1L), anyString());

        mockMvc.perform(delete("/api/inventory/1")
                        .header("X-User-Role", "ADMIN")
                        .header("X-User-Name", "admin"))
                .andExpect(status().isNoContent());

        verify(inventoryService).deleteItem(eq(1L), anyString());
    }

    @Test
    void deleteItem_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(delete("/api/inventory/1")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(inventoryService);
    }

    @Test
    void deleteItem_withoutRoleHeader_returns403() throws Exception {
        mockMvc.perform(delete("/api/inventory/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(inventoryService);
    }

    @Test
    void deleteItem_itemNotFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Item not found with id: 99"))
                .when(inventoryService).deleteItem(eq(99L), anyString());

        mockMvc.perform(delete("/api/inventory/99")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    // ===== GET /api/inventory/low-stock =====

    @Test
    void getLowStockItems_asAdmin_returns200() throws Exception {
        StationeryItemResponse lowStock = StationeryItemResponse.builder()
                .id(2L).name("Eraser").category("ERASER").unit("piece")
                .availableQuantity(2).minimumQuantity(10).lowStock(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        when(inventoryService.getLowStockItems()).thenReturn(List.of(lowStock));

        mockMvc.perform(get("/api/inventory/low-stock")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lowStock").value(true));
    }

    @Test
    void getLowStockItems_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/inventory/low-stock")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(inventoryService);
    }

    @Test
    void getLowStockItems_withoutRoleHeader_returns403() throws Exception {
        mockMvc.perform(get("/api/inventory/low-stock"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(inventoryService);
    }

    // ===== PUT /api/inventory/{id}/deduct =====

    @Test
    void deductQuantity_success_returnsTrue() throws Exception {
        when(inventoryService.deductQuantity(1L, 5)).thenReturn(true);

        mockMvc.perform(put("/api/inventory/1/deduct")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void deductQuantity_insufficientStock_returns400() throws Exception {
        when(inventoryService.deductQuantity(1L, 999))
                .thenThrow(new InsufficientStockException("Insufficient stock for item ID: 1"));

        mockMvc.perform(put("/api/inventory/1/deduct")
                        .param("quantity", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient stock for item ID: 1"));
    }

    @Test
    void deductQuantity_itemNotFound_returns404() throws Exception {
        when(inventoryService.deductQuantity(99L, 5))
                .thenThrow(new ResourceNotFoundException("Item not found with id: 99"));

        mockMvc.perform(put("/api/inventory/99/deduct")
                        .param("quantity", "5"))
                .andExpect(status().isNotFound());
    }

    // ===== GET /api/inventory/search =====

    @Test
    void searchItems_returnsMatchingItems() throws Exception {
        when(inventoryService.searchItems("pen")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/inventory/search")
                        .param("keyword", "pen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Blue Pen"));
    }

    @Test
    void searchItems_noResults_returnsEmptyList() throws Exception {
        when(inventoryService.searchItems("xyz")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/inventory/search")
                        .param("keyword", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void searchItems_unexpectedError_returns500() throws Exception {
        when(inventoryService.searchItems(anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/inventory/search")
                        .param("keyword", "pen"))
                .andExpect(status().isInternalServerError());
    }
}