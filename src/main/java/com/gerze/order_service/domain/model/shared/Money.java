package com.gerze.order_service.domain.model.shared;

import java.math.BigDecimal;

public record Money(
    BigDecimal amount
) {
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }
}
