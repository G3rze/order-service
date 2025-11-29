package com.gerze.order_service.infrastructure.r2dbc.entity;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "order_items", schema = "order_service")
public record OrderItemEntity(
    @Id @Column("id") UUID id,
    @Column("order_id") UUID orderId,
    @Column("product_id") UUID productId,
    @Column("product_name_snapshot") String productNameSnapshot,
    @Column("product_unit_price_snapshot") BigDecimal productUnitPriceSnapshot,
    @Column("quantity") Integer quantity,
    @Column("line_total") BigDecimal lineTotal
) {}
