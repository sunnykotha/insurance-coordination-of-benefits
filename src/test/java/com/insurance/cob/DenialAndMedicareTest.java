package com.insurance.cob;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DenialAndMedicareTest {
    private BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    @Test
    void secondaryCanPayAfterPrimaryDenial() {
        ClaimAdjudication denial = ClaimAdjudication.denied("Not covered by primary");
        PaymentCalculation result = new DenialCoordinationService()
                .calculateAfterPrimaryDenial(
                        amount("1000"),
                        amount("700"),
                        new StandardPaymentStrategy(),
                        denial);

        assertEquals(amount("0"), result.primaryPayment());
        assertEquals(amount("700"), result.secondaryPayment());
        assertEquals(amount("300"), result.patientResponsibility());
    }

    @Test
    void rejectsNonDeniedPrimaryAdjudication() {
        assertThrows(IllegalArgumentException.class, () ->
                new DenialCoordinationService().calculateAfterPrimaryDenial(
                        amount("1000"), amount("700"),
                        new StandardPaymentStrategy(),
                        ClaimAdjudication.approved(amount("100"))));
    }

    @Test
    void workingAgedEmployerThresholdIsTwentyEmployees() {
        MedicareCoordinationService service = new MedicareCoordinationService();

        assertEquals(MedicarePayerPosition.MEDICARE_PRIMARY,
                service.determine(MedicareCoverageType.WORKING_AGED, 19));
        assertEquals(MedicarePayerPosition.EMPLOYER_PLAN_PRIMARY,
                service.determine(MedicareCoverageType.WORKING_AGED, 20));
    }

    @Test
    void disabilityEmployerThresholdIsOneHundredEmployees() {
        MedicareCoordinationService service = new MedicareCoordinationService();

        assertEquals(MedicarePayerPosition.MEDICARE_PRIMARY,
                service.determine(MedicareCoverageType.DISABILITY, 99));
        assertEquals(MedicarePayerPosition.EMPLOYER_PLAN_PRIMARY,
                service.determine(MedicareCoverageType.DISABILITY, 100));
    }

    @Test
    void specialSituationsRequireAdditionalRules() {
        MedicareCoordinationService service = new MedicareCoordinationService();

        assertEquals(MedicarePayerPosition.NEEDS_ADDITIONAL_RULES,
                service.determine(MedicareCoverageType.ESRD, 500));
        assertEquals(MedicarePayerPosition.NEEDS_ADDITIONAL_RULES,
                service.determine(MedicareCoverageType.COBRA, 500));
    }

    @Test
    void rejectsInvalidEmployeeCount() {
        assertThrows(IllegalArgumentException.class, () ->
                new MedicareCoordinationService().determine(
                        MedicareCoverageType.WORKING_AGED, -1));
    }
}