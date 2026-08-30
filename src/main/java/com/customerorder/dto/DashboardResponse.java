package com.customerorder.dto;

import java.math.BigDecimal;

public record DashboardResponse(long customers, long products, long orders, BigDecimal revenue) {}
