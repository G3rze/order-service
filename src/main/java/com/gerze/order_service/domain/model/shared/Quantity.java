package com.gerze.order_service.domain.model.shared;

public record Quantity(
    int value
) {
    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    public static Quantity zero() {
        return new Quantity(0);
    }

    public Quantity subtract(Quantity other) {
        return new Quantity(this.value - other.value);
    }

    public Quantity multiply(int factor) {
        return new Quantity(this.value * factor);
    }
}
