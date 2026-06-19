package com.stationery.request.client;

import com.stationery.request.dto.InventoryItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Feign client used for synchronous service-to-service communication with inventory-service.
// We use this to deduct stock safely during the request approval workflow.
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    /**
     * Fetches a stationery item from the inventory service by item ID.
     *
     * @param id the inventory item ID
     * @return the inventory item response wrapped in ResponseEntity
     */
    @GetMapping("/api/inventory/{id}")
    ResponseEntity<InventoryItemResponse> getInventoryItem(@PathVariable("id") Long id);

    /**
     * Deducts quantity for an inventory item.
     *
     * The return type must match the inventory controller endpoint response type
     * so Feign can deserialize the response correctly.
     * Returns just Boolean (not wrapped in ResponseEntity) to avoid Feign deserialization issues.
     *
     * @param id       the inventory item ID
     * @param quantity the quantity to deduct
     * @return the deduction result (true if successful)
     */
    @PutMapping("/api/inventory/{id}/deduct")
    Boolean deductItemQuantity(
            @PathVariable("id") Long id,
            @RequestParam("quantity") Integer quantity
    );
}
