package com.example.saga.inventory;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ReservationResponse reserve(@RequestBody ReservationRequest request) {
        return inventoryService.reserve(request);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> release(@PathVariable UUID orderId) {
        inventoryService.release(orderId);
        return ResponseEntity.noContent().build();
    }
}
