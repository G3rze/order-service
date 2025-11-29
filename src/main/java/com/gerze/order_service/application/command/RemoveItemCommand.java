package com.gerze.order_service.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record RemoveItemCommand(
    @NotNull UUID orderId,
    @NotNull UUID itemId
) {}
