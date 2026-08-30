package com.king.customerorder.controller;

import com.king.customerorder.dto.DashboardResponse;
import com.king.customerorder.repository.CustomerOrderRepository;
import com.king.customerorder.repository.CustomerRepository;
import com.king.customerorder.repository.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final CustomerRepository customers;
    private final ProductRepository products;
    private final CustomerOrderRepository orders;

    public DashboardController(CustomerRepository customers, ProductRepository products, CustomerOrderRepository orders) {
        this.customers = customers;
        this.products = products;
        this.orders = orders;
    }

    @GetMapping
    public DashboardResponse dashboard() {
        return new DashboardResponse(customers.count(), products.count(), orders.count(), orders.totalRevenue());
    }
}
