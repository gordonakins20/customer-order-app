package com.customerorder.controller;

import com.customerorder.model.Customer;
import com.customerorder.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerRepository repository;

    public CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Customer> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Customer one(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer create(@Valid @RequestBody Customer customer) {
        customer.setId(null);
        if (repository.existsByEmailIgnoreCase(customer.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        return repository.save(customer);
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id, @Valid @RequestBody Customer input) {
        Customer customer = one(id);
        customer.setName(input.getName());
        customer.setEmail(input.getEmail());
        customer.setPhone(input.getPhone());
        return repository.save(customer);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.delete(one(id));
    }
}
