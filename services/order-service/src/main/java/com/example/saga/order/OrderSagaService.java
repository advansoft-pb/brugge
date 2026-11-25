package com.example.saga.order;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderSagaService {
    private final RestClient inventoryClient;
    private final RestClient paymentClient;
    private final Map<UUID, OrderRecord> orders = new ConcurrentHashMap<>();

    public OrderSagaService(
            RestClient.Builder builder,
            @Value("${services.inventory.url:http://localhost:8081}") String inventoryUrl,
            @Value("${services.payment.url:http://localhost:8082}") String paymentUrl) {
        this.inventoryClient = builder.clone().baseUrl(inventoryUrl).build();
        this.paymentClient = builder.clone().baseUrl(paymentUrl).build();
    }

    public OrderResponse create(CreateOrderRequest request) {
        validate(request);
        UUID orderId = UUID.randomUUID();
        OrderRecord order = new OrderRecord(orderId, request);
        orders.put(orderId, order);
        boolean inventoryReserved = false;

        try {
            order.status = "RESERVING_INVENTORY";
            inventoryClient.post().uri("/reservations")
                    .body(new ReservationRequest(orderId, request.itemId(), request.quantity()))
                    .retrieve().toBodilessEntity();
            inventoryReserved = true;

            order.status = "CHARGING_PAYMENT";
            paymentClient.post().uri("/payments/charges")
                    .body(new ChargeRequest(orderId, request.customerId(), request.amount(), request.simulatePaymentFailure()))
                    .retrieve().toBodilessEntity();

            order.status = "CONFIRMED";
        } catch (RuntimeException sagaFailure) {
            order.failure = safeMessage(sagaFailure);
            order.status = "FAILED";
            if (inventoryReserved) {
                try {
                    inventoryClient.delete().uri("/reservations/{orderId}", orderId)
                            .retrieve().toBodilessEntity();
                    order.compensationStatus = "INVENTORY_RELEASED";
                } catch (RuntimeException compensationFailure) {
                    order.status = "COMPENSATION_FAILED";
                    order.compensationStatus = "INVENTORY_RELEASE_FAILED: " + safeMessage(compensationFailure);
                }
            }
        }
        return order.response();
    }

    public OrderResponse get(UUID orderId) {
        OrderRecord order = orders.get(orderId);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return order.response();
    }

    private static void validate(CreateOrderRequest request) {
        if (request == null || request.customerId() == null || request.customerId().isBlank()
                || request.itemId() == null || request.itemId().isBlank()
                || request.quantity() <= 0 || request.amount() == null
                || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "customerId, itemId, positive quantity, and positive amount are required");
        }
    }

    private static String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null ? exception.getClass().getSimpleName() : message;
    }

    private record ReservationRequest(UUID orderId, String itemId, int quantity) { }
    private record ChargeRequest(UUID orderId, String customerId, BigDecimal amount, boolean simulateFailure) { }
}
