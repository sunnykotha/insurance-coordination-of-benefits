package com.insurance.cob;

import java.time.LocalDate;
import java.util.Objects;

public record Policyholder(String id, String name, LocalDate dateOfBirth) {
    public Policyholder {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Policyholder ID is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Policyholder name is required");
        Objects.requireNonNull(dateOfBirth, "Policyholder date of birth is required");
    }
}