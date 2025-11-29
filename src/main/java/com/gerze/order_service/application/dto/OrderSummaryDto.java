package com.gerze.order_service.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryDto(
    UUID id,
    UUID customerId,
    OrderStatusDto status,
    BigDecimal totalAmount,
    Instant createdAt
) {}
