package com.gerze.order_service.application.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderQuery(
    UUID customerId,
    UUID productId,
    String status,
    Instant createdAfter,
    Instant createdBefore,
    BigDecimal minTotalAmount,
    BigDecimal maxTotalAmount
) {}
