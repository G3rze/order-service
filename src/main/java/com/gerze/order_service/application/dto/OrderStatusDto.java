package com.gerze.order_service.application.dto;

import java.util.UUID;

public record OrderStatusDto(
    UUID id,
    String code,
    String label,
    String description,
    boolean isFinal
) {}
