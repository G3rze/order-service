package com.gerze.order_service.domain.model.product;

import java.time.Instant;
import java.util.UUID;

import com.gerze.order_service.domain.model.shared.Money;

public record Product(
    UUID id,
    String name,
    Money price,
    String description,
    Boolean is_active,
    Instant createdAt,
    Instant updatedAt
) {
    public static Product createNewProduct(
        UUID id,
        String name,
        Money price,
        String description,
        Boolean is_active
    ) {
        Instant now = Instant.now();
        return new Product(
            id,
            name,
            price,
            description,
            is_active,
            now,
            now
        );
    }

    public Product withName(String newName) {
        return new Product(
            this.id,
            newName,
            this.price,
            this.description,
            this.is_active,
            this.createdAt,
            Instant.now()
        );
    }

    public Product withPrice(Money newPrice) {
        return new Product(
            this.id,
            this.name,
            newPrice,
            this.description,
            this.is_active,
            this.createdAt,
            Instant.now()
        );
    }

    public Product withDescription(String newDescription) {
        return new Product(
            this.id,
            this.name,
            this.price,
            newDescription,
            this.is_active,
            this.createdAt,
            Instant.now()
        );
    }

    public Product withIsActive(Boolean newIsActive) {
        return new Product(
            this.id,
            this.name,
            this.price,
            this.description,
            newIsActive,
            this.createdAt,
            Instant.now()
        );
    }
}
