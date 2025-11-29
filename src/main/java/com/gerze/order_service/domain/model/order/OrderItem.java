package com.gerze.order_service.domain.model.order;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.gerze.order_service.domain.model.product.Product;
import com.gerze.order_service.domain.model.shared.Money;
import com.gerze.order_service.domain.model.shared.Quantity;

public record OrderItem(
    UUID id,
    Product product,
    UUID orderId,
    Quantity quantity,
    Money lineTotal
) {
    public OrderItem {
        Objects.requireNonNull(id, "Order item id cannot be null");
        Objects.requireNonNull(product, "Product cannot be null");
        Objects.requireNonNull(orderId, "Order id cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(lineTotal, "Line total cannot be null");

        Money expectedLineTotal = product.price().multiply(BigDecimal.valueOf(quantity.value()));
        if (!expectedLineTotal.equals(lineTotal)) {
            throw new IllegalArgumentException("Line total must equal product price multiplied by quantity");
        }
    }

    public static OrderItem createNewOrderItem(UUID id, Product product, UUID orderId, Quantity quantity) {
        Money lineTotal = product.price().multiply(BigDecimal.valueOf(quantity.value()));
        return new OrderItem(
            id,
            product,
            orderId,
            quantity,
            lineTotal
        );
    }
}
