package com.gerze.order_service.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ProductRequests {

    private ProductRequests() {}

    public record CreateProductRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal price,
        String description,
        Boolean isActive
    ) {}

    public record UpdateProductRequest(
        String name,
        @Positive BigDecimal price,
        String description,
        Boolean isActive
    ) {}

    public record ProductPath(
        @NotNull UUID id
    ) {}
}
