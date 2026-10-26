package com.insurance.cob;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record Claim(
        String id,
        Patient patient,
        LocalDate serviceDate,
        BigDecimal allowedAmount) {

    public Claim {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Claim ID is required");
        Objects.requireNonNull(patient, "Claim patient is required");
        Objects.requireNonNull(serviceDate, "Service date is required");
        Objects.requireNonNull(allowedAmount, "Allowed amount is required");

        if (allowedAmount.signum() < 0) {
            throw new IllegalArgumentException("Allowed amount cannot be negative");
        }
    }
}