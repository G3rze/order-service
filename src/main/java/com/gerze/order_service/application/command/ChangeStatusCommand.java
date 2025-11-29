package com.gerze.order_service.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ChangeStatusCommand(
    @NotNull UUID orderId,
    @NotNull UUID newStatusId
) {}
