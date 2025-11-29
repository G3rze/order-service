package com.gerze.order_service.infrastructure.r2dbc.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "orders", schema = "order_service")
public record OrderEntity(
    @Id @Column("id") UUID id,
    @Column("customer_id") UUID customerId,
    @Column("status_id") UUID statusId,
    @Column("total_amount") BigDecimal totalAmount,
    @Column("created_at") Instant createdAt,
    @Column("updated_at") Instant updatedAt
) {}
