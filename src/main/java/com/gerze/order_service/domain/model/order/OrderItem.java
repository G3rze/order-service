package com.gerze.order_service.domain.model.order;

import java.math.BigDecimal;
import java.util.UUID;

import com.gerze.order_service.domain.model.product.Product;
import com.gerze.order_service.domain.model.shared.Money;
import com.gerze.order_service.domain.model.shared.Quantity;

public record OrderItem (
    UUID id,
    Product product,
    Order order,
    Quantity quantity,
    Money lineTotal
) {

    public static OrderItem createNewOrderItem(UUID id, Product product, Order order, Quantity quantity) {
        Money lineTotal = product.price().multiply(BigDecimal.valueOf(quantity.value()));
        return new OrderItem(
            id,
            product,
            order,
            quantity,
            lineTotal
        );
    }

    public OrderItem withQuantity(Quantity newQuantity) {
        Money newLineTotal = product.price().multiply(BigDecimal.valueOf(newQuantity.value()));
        return new OrderItem(
            this.id,
            this.product,
            this.order,
            newQuantity,
            newLineTotal
        );
    }

    public OrderItem withProduct(Product newProduct) {
        Money newLineTotal = newProduct.price().multiply(BigDecimal.valueOf(this.quantity.value()));
        return new OrderItem(
            this.id,
            newProduct,
            this.order,
            this.quantity,
            newLineTotal
        );
    }
    
}
