package com.insurance.cob;

import java.math.BigDecimal;
import java.util.Objects;

public final class StandardPaymentStrategy implements PaymentStrategy {
    @Override
    public BigDecimal calculateSecondaryPayment(
            BigDecimal allowedAmount,
            BigDecimal primaryPayment,
            BigDecimal secondaryBenefit) {
        validate(allowedAmount, primaryPayment, secondaryBenefit);
        BigDecimal remaining = allowedAmount.subtract(primaryPayment).max(BigDecimal.ZERO);
        return secondaryBenefit.min(remaining);
    }

    static void validate(
            BigDecimal allowedAmount,
            BigDecimal primaryPayment,
            BigDecimal secondaryBenefit) {
        Objects.requireNonNull(allowedAmount, "Allowed amount is required");
        Objects.requireNonNull(primaryPayment, "Primary payment is required");
        Objects.requireNonNull(secondaryBenefit, "Secondary benefit is required");
        if (allowedAmount.signum() < 0
                || primaryPayment.signum() < 0
                || secondaryBenefit.signum() < 0) {
            throw new IllegalArgumentException("Payment amounts cannot be negative");
        }
        if (primaryPayment.compareTo(allowedAmount) > 0) {
            throw new IllegalArgumentException("Primary payment cannot exceed allowed amount");
        }
    }
}