package com.customerorder.config;

import com.customerorder.model.Customer;
import com.customerorder.model.Product;
import com.customerorder.repository.CustomerRepository;
import com.customerorder.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(CustomerRepository customers, ProductRepository products) {
        return args -> {
            if (customers.count() == 0) {
                customers.save(new Customer("Gordon Student", "gordon@example.com", "555-0101"));
                customers.save(new Customer("Avery Johnson", "avery@example.com", "555-0102"));
            }

            if (products.count() == 0) {
                products.save(new Product("Wireless Keyboard", "KB-100", new BigDecimal("49.99"), 20));
                products.save(new Product("USB-C Hub", "HUB-200", new BigDecimal("34.50"), 15));
                products.save(new Product("Laptop Stand", "STAND-300", new BigDecimal("39.00"), 12));
            }
        };
    }
}
