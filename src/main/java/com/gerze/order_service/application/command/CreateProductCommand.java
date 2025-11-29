package com.gerze.order_service.application.command;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateProductCommand(
    UUID id,
    @NotBlank String name,
    @NotNull @Positive BigDecimal price,
    String description,
    boolean isActive
) {}
