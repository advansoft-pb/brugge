package com.example.saga.inventory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {
    private final Map<UUID, ReservationResponse> reservations = new ConcurrentHashMap<>();

    public ReservationResponse reserve(ReservationRequest request) {
        if (request == null || request.orderId() == null || request.itemId() == null || request.itemId().isBlank()
                || request.quantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid orderId, itemId, and positive quantity are required");
        }
        if ("out-of-stock".equalsIgnoreCase(request.itemId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item is out of stock (demo failure)");
        }
        return reservations.compute(request.orderId(), (orderId, existing) -> {
            if (existing != null && (!existing.itemId().equals(request.itemId()) || existing.quantity() != request.quantity())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already has a different reservation");
            }
            return existing != null ? existing : new ReservationResponse(orderId, request.itemId(),
                    request.quantity(), "RESERVED", Instant.now());
        });
    }

    public void release(UUID orderId) {
        reservations.remove(orderId);
    }
}
