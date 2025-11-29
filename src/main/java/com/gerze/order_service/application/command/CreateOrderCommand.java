package com.gerze.order_service.application.command;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateOrderCommand(
    @NotNull UUID customerId,
    @NotEmpty List<@Valid CreateOrderItemCommand> items,
    @NotNull UUID initialStatusId
) {}
