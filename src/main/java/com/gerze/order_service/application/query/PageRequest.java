package com.gerze.order_service.application.query;

import com.gerze.order_service.domain.spec.SortedBy;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PageRequest(
    @PositiveOrZero int page,
    @Positive int size,
    @NotNull SortedBy sortedBy
) {}
