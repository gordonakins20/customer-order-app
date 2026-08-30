package com.king.customerorder.repository;

import com.king.customerorder.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    @Query("select coalesce(sum(o.total), 0) from CustomerOrder o where o.status <> com.king.customerorder.model.OrderStatus.CANCELLED")
    BigDecimal totalRevenue();
}
