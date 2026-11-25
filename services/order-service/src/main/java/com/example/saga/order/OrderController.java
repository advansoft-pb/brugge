package com.example.saga.order;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderSagaService sagaService;

    public OrderController(OrderSagaService sagaService) {
        this.sagaService = sagaService;
    }

    @PostMapping
    public OrderResponse create(@RequestBody CreateOrderRequest request) {
        return sagaService.create(request);
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable String orderId) {
        try {
            return sagaService.get(UUID.fromString(orderId));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderId must be a UUID");
        }
    }
}
