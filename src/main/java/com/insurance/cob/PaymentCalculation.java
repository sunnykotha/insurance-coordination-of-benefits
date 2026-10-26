package com.insurance.cob;

import java.math.BigDecimal;
import java.util.Objects;

public record PaymentCalculation(
        BigDecimal allowedAmount,
        BigDecimal primaryPayment,
        BigDecimal secondaryPayment,
        BigDecimal patientResponsibility) {

    public PaymentCalculation {
        Objects.requireNonNull(allowedAmount, "Allowed amount is required");
        Objects.requireNonNull(primaryPayment, "Primary payment is required");
        Objects.requireNonNull(secondaryPayment, "Secondary payment is required");
        Objects.requireNonNull(patientResponsibility, "Patient responsibility is required");

        if (allowedAmount.signum() < 0
                || primaryPayment.signum() < 0
                || secondaryPayment.signum() < 0
                || patientResponsibility.signum() < 0) {
            throw new IllegalArgumentException("Payment amounts cannot be negative");
        }

        if (primaryPayment.add(secondaryPayment).compareTo(allowedAmount) > 0) {
            throw new IllegalArgumentException("Combined payments cannot exceed allowed amount");
        }

        if (primaryPayment.add(secondaryPayment).add(patientResponsibility)
                .compareTo(allowedAmount) != 0) {
            throw new IllegalArgumentException(
                    "Payments and patient responsibility must equal allowed amount");
        }
    }
}