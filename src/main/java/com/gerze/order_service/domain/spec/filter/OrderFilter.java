package com.gerze.order_service.domain.spec.filter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderFilter(
    UUID customerId,
    UUID productId,
    String status,
    Instant createdAfter,
    Instant createdBefore,
    BigDecimal minTotalAmount,
    BigDecimal maxTotalAmount
) {}
