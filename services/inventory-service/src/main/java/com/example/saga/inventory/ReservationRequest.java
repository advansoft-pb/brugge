package com.example.saga.inventory;

import java.util.UUID;

public record ReservationRequest(UUID orderId, String itemId, int quantity) { }
