package com.gerze.order_service.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderItemCommand(
    @NotNull UUID productId,
    @Positive int quantity
) {}
