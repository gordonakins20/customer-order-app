package com.king.customerorder.service;

import com.king.customerorder.dto.CreateOrderRequest;
import com.king.customerorder.model.*;
import com.king.customerorder.repository.CustomerOrderRepository;
import com.king.customerorder.repository.CustomerRepository;
import com.king.customerorder.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private final CustomerOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(CustomerOrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    public List<CustomerOrder> findAll() {
        return orderRepository.findAll();
    }

    public CustomerOrder findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    @Transactional
    public CustomerOrder create(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        CustomerOrder order = new CustomerOrder();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.NEW);

        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderRequest.ItemRequest requestedItem : request.getItems()) {
            Product product = productRepository.findById(requestedItem.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + requestedItem.getProductId()));

            int quantity = requestedItem.getQuantity();
            if (product.getStockQuantity() < quantity) {
                throw new IllegalArgumentException("Not enough stock for " + product.getName());
            }

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            item.setLineTotal(lineTotal);
            order.addItem(item);

            product.setStockQuantity(product.getStockQuantity() - quantity);
            total = total.add(lineTotal);
        }

        order.setTotal(total);
        return orderRepository.save(order);
    }

    @Transactional
    public CustomerOrder updateStatus(Long id, OrderStatus newStatus) {
        CustomerOrder order = findById(id);

        if (order.getStatus() != OrderStatus.CANCELLED && newStatus == OrderStatus.CANCELLED) {
            restock(order);
        } else if (order.getStatus() == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
            removeStock(order);
        }

        order.setStatus(newStatus);
        return order;
    }

    @Transactional
    public void delete(Long id) {
        CustomerOrder order = findById(id);
        if (order.getStatus() != OrderStatus.CANCELLED) {
            restock(order);
        }
        orderRepository.delete(order);
    }

    private void restock(CustomerOrder order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }
    }

    private void removeStock(CustomerOrder order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock to reopen order for " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
        }
    }
}
