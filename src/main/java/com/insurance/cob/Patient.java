package com.insurance.cob;

import java.time.LocalDate;
import java.util.Objects;

public record Patient(String id, String name, LocalDate dateOfBirth) {
    public Patient {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Patient ID is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Patient name is required");
        Objects.requireNonNull(dateOfBirth, "Patient date of birth is required");
    }
}