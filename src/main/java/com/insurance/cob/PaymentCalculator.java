package com.insurance.cob;

import java.math.BigDecimal;
import java.util.Objects;

public final class PaymentCalculator {

    public PaymentCalculation calculate(
            BigDecimal allowedAmount,
            BigDecimal primaryPayment,
            BigDecimal secondaryBenefit,
            PaymentStrategy secondaryStrategy) {

        Objects.requireNonNull(allowedAmount, "Allowed amount is required");
        Objects.requireNonNull(primaryPayment, "Primary payment is required");
        Objects.requireNonNull(secondaryBenefit, "Secondary benefit is required");
        Objects.requireNonNull(secondaryStrategy, "Secondary strategy is required");

        if (allowedAmount.signum() < 0
                || primaryPayment.signum() < 0
                || secondaryBenefit.signum() < 0) {
            throw new IllegalArgumentException("Payment amounts cannot be negative");
        }
        if (primaryPayment.compareTo(allowedAmount) > 0) {
            throw new IllegalArgumentException("Primary payment cannot exceed allowed amount");
        }

        BigDecimal secondaryPayment = secondaryStrategy.calculateSecondaryPayment(
                allowedAmount, primaryPayment, secondaryBenefit);

        BigDecimal remaining = allowedAmount.subtract(primaryPayment);
        secondaryPayment = secondaryPayment.min(remaining).max(BigDecimal.ZERO);

        BigDecimal patientResponsibility = allowedAmount
                .subtract(primaryPayment)
                .subtract(secondaryPayment);

        return new PaymentCalculation(
                allowedAmount, primaryPayment, secondaryPayment, patientResponsibility);
    }

    public PaymentCalculation calculatePrimaryOnly(
            BigDecimal allowedAmount,
            BigDecimal primaryPayment) {
        Objects.requireNonNull(allowedAmount, "Allowed amount is required");
        Objects.requireNonNull(primaryPayment, "Primary payment is required");

        if (allowedAmount.signum() < 0 || primaryPayment.signum() < 0) {
            throw new IllegalArgumentException("Payment amounts cannot be negative");
        }
        if (primaryPayment.compareTo(allowedAmount) > 0) {
            throw new IllegalArgumentException("Primary payment cannot exceed allowed amount");
        }

        return new PaymentCalculation(
                allowedAmount,
                primaryPayment,
                BigDecimal.ZERO,
                allowedAmount.subtract(primaryPayment));
    }
}