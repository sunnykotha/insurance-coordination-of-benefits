package com.insurance.cob;

import java.time.LocalDate;
import java.util.Objects;

public record CourtOrder(
        String patientId,
        String primaryPolicyholderId,
        LocalDate effectiveFrom,
        LocalDate effectiveThrough) {

    public CourtOrder {
        if (patientId == null || patientId.isBlank())
            throw new IllegalArgumentException("Patient ID is required");
        if (primaryPolicyholderId == null || primaryPolicyholderId.isBlank())
            throw new IllegalArgumentException("Primary policyholder ID is required");

        Objects.requireNonNull(effectiveFrom, "Effective start date is required");

        if (effectiveThrough != null && effectiveThrough.isBefore(effectiveFrom))
            throw new IllegalArgumentException("Court order end date cannot precede start date");
    }

    public boolean appliesTo(Patient patient, LocalDate date) {
        Objects.requireNonNull(patient, "Patient is required");
        Objects.requireNonNull(date, "Date is required");

        return patient.id().equals(patientId)
                && !date.isBefore(effectiveFrom)
                && (effectiveThrough == null || !date.isAfter(effectiveThrough));
    }
}