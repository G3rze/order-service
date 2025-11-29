package com.gerze.order_service.infrastructure.rest.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OrderRequests {

    private OrderRequests() {}

    public record CreateOrderRequest(
        @NotNull UUID customerId,
        @NotNull UUID initialStatusId,
        @NotEmpty List<@Valid CreateOrderItemRequest> items
    ) {}

    public record CreateOrderItemRequest(
        @NotNull UUID productId,
        @Positive int quantity
    ) {}

    public record AddItemRequest(
        @NotNull UUID productId,
        @Positive int quantity
    ) {}

    public record UpdateQuantityRequest(
        @Positive int quantity
    ) {}

    public record ChangeStatusRequest(
        @NotNull UUID newStatusId
    ) {}
}
