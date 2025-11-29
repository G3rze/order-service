package com.gerze.order_service.infrastructure.r2dbc.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "product", schema = "order_service")
public record ProductEntity(
    @Id @Column("id") UUID id,
    @Column("name") String name,
    @Column("price") BigDecimal price,
    @Column("description") String description,
    @Column("is_active") Boolean isActive,
    @Column("created_at") Instant createdAt,
    @Column("updated_at") Instant updatedAt
) {}
