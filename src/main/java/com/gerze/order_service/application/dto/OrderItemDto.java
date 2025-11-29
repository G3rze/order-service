package com.gerze.order_service.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(
    UUID id,
    UUID productId,
    int quantity,
    BigDecimal lineTotal
) {}
