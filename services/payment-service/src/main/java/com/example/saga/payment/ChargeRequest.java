package com.example.saga.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record ChargeRequest(UUID orderId, String customerId, BigDecimal amount, boolean simulateFailure) { }
