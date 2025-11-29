package com.gerze.order_service.domain.model.shared;

import java.math.BigDecimal;

public record Money(
    BigDecimal amount
) {

    public Money {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money amount cannot be negative");
        }
    }

    public Money add(Money other) {
        BigDecimal newAmount = this.amount.add(other.amount);

        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Resulting money amount cannot be negative");
        }

        return new Money(newAmount);
    }

    public Money multiply(BigDecimal factor) {
        BigDecimal newAmount = this.amount.multiply(factor);

        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Resulting money amount cannot be negative");
        }

        return new Money(newAmount);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money subtract(Money other) {
        BigDecimal newAmount = this.amount.subtract(other.amount);

        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Resulting money amount cannot be negative");
        }

        return new Money(this.amount.subtract(other.amount));
    }
}
