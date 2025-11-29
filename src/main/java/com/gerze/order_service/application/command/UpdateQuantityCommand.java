package com.gerze.order_service.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateQuantityCommand(
    @NotNull UUID orderId,
    @NotNull UUID itemId,
    @Positive int quantity
) {}
