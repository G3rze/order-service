package com.gerze.order_service.domain.model.order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.gerze.order_service.domain.model.shared.Money;

public record Order(
    UUID id,
    UUID customerId,
    OrderStatus status,
    List<OrderItem> items,
    Money totalAmount,
    Instant createdAt,
    Instant updatedAt
) {

    public static Order createNewOrder(UUID id,UUID customerId, List<OrderItem> items, Money totalAmount, OrderStatus initialStatus) {
        return new Order(
            id,
            customerId,
            initialStatus,
            items,
            totalAmount,
            Instant.now(),
            Instant.now()
        );
    }

    public Order withStatus(OrderStatus newStatus) {
        return new Order(
            this.id,
            this.customerId,
            newStatus,
            this.items,
            this.totalAmount,
            this.createdAt,
            Instant.now()
        );
    }

    public Order withItems(List<OrderItem> newItems) {
        return new Order(
            this.id,
            this.customerId,
            this.status,
            List.copyOf(newItems),
            calculateTotalAmount(),
            this.createdAt,
            Instant.now()
        );
    }

    public Boolean isFinalized() {
        return this.status.isFinal();
    }

    public Money calculateTotalAmount() {
        return items.stream()
                    .map(OrderItem::lineTotal)
                    .reduce(Money.zero(), Money::add);
    }
}
