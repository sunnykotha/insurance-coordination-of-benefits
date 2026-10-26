package com.insurance.cob;

import java.math.BigDecimal;
import java.util.Objects;

public record ClaimAdjudication(
        boolean denied,
        BigDecimal payment,
        String reason) {

    public ClaimAdjudication {
        Objects.requireNonNull(payment, "Payment is required");
        if (payment.signum() < 0) {
            throw new IllegalArgumentException("Payment cannot be negative");
        }
        if (denied && payment.signum() != 0) {
            throw new IllegalArgumentException("A denied claim cannot have a payment");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Adjudication reason is required");
        }
    }

    public static ClaimAdjudication approved(BigDecimal payment) {
        return new ClaimAdjudication(false, payment, "Approved");
    }

    public static ClaimAdjudication denied(String reason) {
        return new ClaimAdjudication(true, BigDecimal.ZERO, reason);
    }
}