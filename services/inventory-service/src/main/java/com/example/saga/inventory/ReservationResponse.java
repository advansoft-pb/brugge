package com.example.saga.inventory;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(UUID orderId, String itemId, int quantity, String status, Instant createdAt) { }
