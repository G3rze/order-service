package com.gerze.order_service.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductDto(
    UUID id,
    String name,
    BigDecimal price,
    String description,
    boolean isActive,
    Instant createdAt,
    Instant updatedAt
) {}
