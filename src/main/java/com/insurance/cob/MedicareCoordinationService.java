package com.insurance.cob;

public final class MedicareCoordinationService {
    public MedicarePayerPosition determine(
            MedicareCoverageType coverageType,
            int employerEmployeeCount) {

        if (coverageType == null) {
            throw new IllegalArgumentException("Medicare coverage type is required");
        }
        if (employerEmployeeCount < 0) {
            throw new IllegalArgumentException("Employee count cannot be negative");
        }

        return switch (coverageType) {
            case WORKING_AGED -> employerEmployeeCount >= 20
                    ? MedicarePayerPosition.EMPLOYER_PLAN_PRIMARY
                    : MedicarePayerPosition.MEDICARE_PRIMARY;
            case DISABILITY -> employerEmployeeCount >= 100
                    ? MedicarePayerPosition.EMPLOYER_PLAN_PRIMARY
                    : MedicarePayerPosition.MEDICARE_PRIMARY;
            case ESRD, RETIREE, COBRA, OTHER ->
                    MedicarePayerPosition.NEEDS_ADDITIONAL_RULES;
        };
    }
}