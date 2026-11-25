package com.example.saga.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

final class OrderRecord {
    final UUID orderId;
    final String customerId;
    final String itemId;
    final int quantity;
    final BigDecimal amount;
    final Instant createdAt = Instant.now();
    volatile String status = "PENDING";
    volatile String failure;
    volatile String compensationStatus = "NOT_REQUIRED";

    OrderRecord(UUID orderId, CreateOrderRequest request) {
        this.orderId = orderId;
        this.customerId = request.customerId();
        this.itemId = request.itemId();
        this.quantity = request.quantity();
        this.amount = request.amount();
    }

    OrderResponse response() {
        return new OrderResponse(orderId, customerId, itemId, quantity, amount,
                status, failure, compensationStatus, createdAt);
    }
}
