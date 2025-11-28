package com.gerze.order_service.domain.model.order;

import java.time.Instant;
import java.util.UUID;

public record OrderStatus (
    UUID id,
    String code,
    String label,
    String description,
    Boolean isFinal,
    Instant createdAt
) {
    public static OrderStatus createNewOrderStatus(
        UUID id,
        String code,
        String label,
        String description,
        Boolean isFinal
    ) {
        return new OrderStatus(
            id,
            code,
            label,
            description,
            isFinal,
            Instant.now()
        );
    }

    public OrderStatus withLabel(String newLabel) {
        return new OrderStatus(
            this.id,
            this.code,
            newLabel,
            this.description,
            this.isFinal,
            this.createdAt
        );
    }

    public OrderStatus withDescription(String newDescription) {
        return new OrderStatus(
            this.id,
            this.code,
            this.label,
            newDescription,
            this.isFinal,
            this.createdAt
        );
    }

    public OrderStatus withIsFinal(Boolean newIsFinal) {
        return new OrderStatus(
            this.id,
            this.code,
            this.label,
            this.description,
            newIsFinal,
            this.createdAt
        );
    }

    public OrderStatus withCode(String newCode) {
        return new OrderStatus(
            this.id,
            newCode,
            this.label,
            this.description,
            this.isFinal,
            this.createdAt
        );
    }
}
