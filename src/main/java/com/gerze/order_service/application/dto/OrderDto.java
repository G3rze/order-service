package com.gerze.order_service.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDto(
    UUID id,
    UUID customerId,
    OrderStatusDto status,
    List<OrderItemDto> items,
    BigDecimal totalAmount,
    Instant createdAt,
    Instant updatedAt
) {}
