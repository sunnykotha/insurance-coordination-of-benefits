package com.insurance.cob;

import java.math.BigDecimal;
import java.util.Objects;

public final class CarveOutPaymentStrategy implements PaymentStrategy {
    private final BigDecimal carveOutAmount;

    public CarveOutPaymentStrategy(BigDecimal carveOutAmount) {
        Objects.requireNonNull(carveOutAmount, "Carve-out amount is required");
        if (carveOutAmount.signum() < 0) {
            throw new IllegalArgumentException("Carve-out amount cannot be negative");
        }
        this.carveOutAmount = carveOutAmount;
    }

    @Override
    public BigDecimal calculateSecondaryPayment(
            BigDecimal allowedAmount,
            BigDecimal primaryPayment,
            BigDecimal secondaryBenefit) {
        StandardPaymentStrategy.validate(allowedAmount, primaryPayment, secondaryBenefit);
        BigDecimal remaining = allowedAmount.subtract(primaryPayment).max(BigDecimal.ZERO);
        return carveOutAmount.min(secondaryBenefit).min(remaining);
    }
}