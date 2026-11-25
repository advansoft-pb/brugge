package com.example.saga.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {
    private final Map<UUID, ChargeResponse> charges = new ConcurrentHashMap<>();

    public ChargeResponse charge(ChargeRequest request) {
        if (request == null || request.orderId() == null || request.customerId() == null || request.customerId().isBlank()
                || request.amount() == null || request.amount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid orderId, customerId, and positive amount are required");
        }
        if (request.simulateFailure()) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Payment declined (simulated demo failure)");
        }
        return charges.computeIfAbsent(request.orderId(), orderId ->
                new ChargeResponse(orderId, "CHARGED", request.amount(), Instant.now()));
    }
}
