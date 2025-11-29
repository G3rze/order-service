package com.gerze.order_service.infrastructure.r2dbc.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "order_status", schema = "order_service")
public record OrderStatusEntity(
    @Id @Column("id") UUID id,
    @Column("code") String code,
    @Column("label") String label,
    @Column("description") String description,
    @Column("is_final") Boolean isFinal,
    @Column("created_at") Instant createdAt
) {}
