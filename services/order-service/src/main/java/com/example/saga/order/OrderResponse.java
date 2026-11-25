package com.example.saga.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String customerId,
        String itemId,
        int quantity,
        BigDecimal amount,
        String status,
        String failure,
        String compensationStatus,
        Instant createdAt) {
}
