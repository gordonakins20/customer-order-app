package com.customerorder.controller;

import com.customerorder.dto.CreateOrderRequest;
import com.customerorder.model.CustomerOrder;
import com.customerorder.model.OrderStatus;
import com.customerorder.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerOrder> all() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CustomerOrder one(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOrder create(@Valid @RequestBody CreateOrderRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}/status")
    public CustomerOrder updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String value = body.get("status");
        if (value == null) {
            throw new IllegalArgumentException("status is required");
        }
        return service.updateStatus(id, OrderStatus.valueOf(value.toUpperCase()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
