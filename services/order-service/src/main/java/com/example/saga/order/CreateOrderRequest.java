package com.example.saga.order;

import java.math.BigDecimal;

public record CreateOrderRequest(
        String customerId,
        String itemId,
        int quantity,
        BigDecimal amount,
        boolean simulatePaymentFailure) {
}
