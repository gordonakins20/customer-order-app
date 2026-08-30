package com.customerorder.repository;

import com.customerorder.model.CustomerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "customer",
            "items",
            "items.product"
    })
    List<CustomerOrder> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "customer",
            "items",
            "items.product"
    })
    Optional<CustomerOrder> findById(Long id);

    @Query("""
        select coalesce(sum(o.total), 0)
        from CustomerOrder o
        where o.status = com.customerorder.model.OrderStatus.COMPLETED
        """)
    BigDecimal totalRevenue();
}