package com.gerze.order_service.application.command;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateProductCommand(
    @NotNull UUID id,
    String name,
    @Positive BigDecimal price,
    String description,
    Boolean isActive
) {}
