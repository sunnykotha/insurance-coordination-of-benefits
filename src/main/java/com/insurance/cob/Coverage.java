package com.insurance.cob;

import java.time.LocalDate;
import java.util.Objects;

public record Coverage(
        String id,
        Patient patient,
        Policyholder policyholder,
        Plan plan,
        CoverageRelationship relationship,
        CoverageType coverageType,
        LocalDate startDate,
        LocalDate endDate) {

    public Coverage {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Coverage ID is required");
        Objects.requireNonNull(patient, "Patient is required");
        Objects.requireNonNull(policyholder, "Policyholder is required");
        Objects.requireNonNull(plan, "Plan is required");
        Objects.requireNonNull(relationship, "Coverage relationship is required");
        Objects.requireNonNull(coverageType, "Coverage type is required");
        Objects.requireNonNull(startDate, "Coverage start date is required");

        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Coverage end date cannot precede start date");
        }

        if (relationship == CoverageRelationship.SELF
                && !patient.id().equals(policyholder.id())) {
            throw new IllegalArgumentException("SELF coverage requires the patient to be the policyholder");
        }

        if (relationship != CoverageRelationship.SELF
                && patient.id().equals(policyholder.id())) {
            throw new IllegalArgumentException("Dependent coverage requires a different policyholder");
        }
    }

    public boolean isActiveOn(LocalDate date) {
        Objects.requireNonNull(date, "Date is required");
        return !date.isBefore(startDate)
                && (endDate == null || !date.isAfter(endDate));
    }
}