package com.gerze.order_service.domain.model.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.gerze.order_service.domain.model.shared.Money;
import com.gerze.order_service.domain.model.shared.Quantity;

public record Order(
    UUID id,
    UUID customerId,
    OrderStatus status,
    List<OrderItem> items,
    Money totalAmount,
    Instant createdAt,
    Instant updatedAt
) {

    public static Order createNewOrder(UUID id, UUID customerId, List<OrderItem> items, OrderStatus initialStatus) {
        List<OrderItem> safeItems = List.copyOf(Objects.requireNonNull(items, "Items cannot be null"));
        return new Order(
            id,
            customerId,
            initialStatus,
            safeItems,
            calculateTotalAmount(safeItems),
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
        List<OrderItem> safeItems = List.copyOf(Objects.requireNonNull(newItems, "Items cannot be null"));
        return new Order(
            this.id,
            this.customerId,
            this.status,
            safeItems,
            calculateTotalAmount(safeItems),
            this.createdAt,
            Instant.now()
        );
    }

    public Order addItem(OrderItem newItem) {
        Objects.requireNonNull(newItem, "New item cannot be null");
        if (this.items.stream().anyMatch(item -> item.id().equals(newItem.id()))) {
            throw new IllegalArgumentException("Item with the same id already exists in the order");
        }

        List<OrderItem> updatedItems = new ArrayList<>(this.items);
        updatedItems.add(newItem);
        return withItems(updatedItems);
    }

    public Order removeItem(UUID itemId) {
        Objects.requireNonNull(itemId, "Item id cannot be null");
        List<OrderItem> updatedItems = this.items.stream()
            .filter(item -> !item.id().equals(itemId))
            .toList();

        if (updatedItems.size() == this.items.size()) {
            throw new IllegalArgumentException("Item to remove was not found in the order");
        }

        return withItems(updatedItems);
    }

    public Order updateItemQuantity(UUID itemId, Quantity newQuantity) {
        Objects.requireNonNull(itemId, "Item id cannot be null");
        Objects.requireNonNull(newQuantity, "Quantity cannot be null");

        List<OrderItem> updatedItems = this.items.stream()
            .map(item -> {
                if (item.id().equals(itemId)) {
                    return OrderItem.createNewOrderItem(
                        item.id(),
                        item.product(),
                        item.orderId(),
                        newQuantity
                    );
                }
                return item;
            })
            .toList();

        boolean itemUpdated = updatedItems.stream()
            .anyMatch(item -> item.id().equals(itemId) && item.quantity().equals(newQuantity));
        if (!itemUpdated) {
            throw new IllegalArgumentException("Item to update was not found in the order");
        }

        return withItems(updatedItems);
    }

    public Boolean isFinalized() {
        return this.status.isFinal();
    }

    private static Money calculateTotalAmount(List<OrderItem> items) {
        return items.stream()
                    .map(OrderItem::lineTotal)
                    .reduce(Money.zero(), Money::add);
    }
}
