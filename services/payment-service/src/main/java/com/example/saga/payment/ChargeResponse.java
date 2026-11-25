package com.example.saga.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ChargeResponse(UUID orderId, String status, BigDecimal amount, Instant createdAt) { }
