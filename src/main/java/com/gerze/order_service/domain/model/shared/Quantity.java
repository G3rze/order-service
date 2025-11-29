package com.gerze.order_service.domain.model.shared;

public record Quantity(
    int value
) {
    public Quantity {
        if (value < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }

    public Quantity add(Quantity other) {
        int newValue = this.value + other.value;
        if (newValue < 0) {
            throw new IllegalArgumentException("Resulting quantity cannot be negative");
        }
        return new Quantity(newValue);
    }

    public static Quantity zero() {
        return new Quantity(0);
    }

    public Quantity subtract(Quantity other) {
        if (this.value < other.value) {
            throw new IllegalArgumentException("Resulting quantity cannot be negative");
        }
        return new Quantity(this.value - other.value);
    }

    public Quantity multiply(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Factor cannot be negative");
        }
        return new Quantity(this.value * factor);
    }
}
