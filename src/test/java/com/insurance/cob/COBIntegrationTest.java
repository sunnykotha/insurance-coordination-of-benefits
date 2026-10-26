package com.insurance.cob;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class COBIntegrationTest {

    private final COBDeterminationService determinationService =
            new COBDeterminationService();
    private final PaymentCalculator paymentCalculator =
            new PaymentCalculator();

    private BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    private final Patient child = new Patient(
            "CHILD-INT", "Integration Child", LocalDate.of(2015, 6, 10));

    private final Policyholder parentA = new Policyholder(
            "PARENT-A", "Parent A", LocalDate.of(1980, 3, 10));

    private final Policyholder parentB = new Policyholder(
            "PARENT-B", "Parent B", LocalDate.of(1978, 2, 20));

    private Coverage coverage(
            String id,
            Policyholder holder,
            String planId,
            CoverageRelationship relationship,
            CoverageType type,
            LocalDate start,
            LocalDate end,
            CobMethod method) {
        return new Coverage(
                id, child, holder,
                new Plan(planId, planId, method),
                relationship, type, start, end);
    }

    private Claim claim(LocalDate date, String allowed) {
        return new Claim("CLAIM-" + date, child, date, amount(allowed));
    }

    @Test
    void fullFlowDeterminesPrimaryAndCalculatesStandardSecondaryPayment() {
        Coverage planA = coverage(
                "COV-A", parentA, "PLAN-A",
                CoverageRelationship.CHILD, CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2026, 1, 1), null, CobMethod.STANDARD);
        Coverage planB = coverage(
                "COV-B", parentB, "PLAN-B",
                CoverageRelationship.CHILD, CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2026, 1, 1), null, CobMethod.STANDARD);

        COBDetermination determination = determinationService.determine(
                claim(LocalDate.of(2026, 5, 1), "1000"),
                List.of(planB, planA));

        assertEquals("PLAN-B", determination.primaryPlan().coverage().plan().id());

        PaymentCalculation result = paymentCalculator.calculate(
                amount("1000"), amount("800"), amount("700"),
                new StandardPaymentStrategy());

        assertEquals(amount("200"), result.secondaryPayment());
        assertEquals(amount("0"), result.patientResponsibility());
    }

    @Test
    void threePlansCanBeOrderedAndPaymentsRemainWithinAllowedAmount() {
        Policyholder parentC = new Policyholder(
                "PARENT-C", "Parent C", LocalDate.of(1982, 1, 15));

        Coverage planA = coverage(
                "COV-A", parentA, "PLAN-A",
                CoverageRelationship.CHILD, CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2026, 1, 1), null, CobMethod.STANDARD);
        Coverage planB = coverage(
                "COV-B", parentB, "PLAN-B",
                CoverageRelationship.CHILD, CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2026, 1, 1), null, CobMethod.STANDARD);
        Coverage planC = coverage(
                "COV-C", parentC, "PLAN-C",
                CoverageRelationship.CHILD, CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2026, 1, 1), null, CobMethod.STANDARD);

        COBDetermination determination = determinationService.determine(
                claim(LocalDate.of(2026, 5, 1), "1000"),
                List.of(planA, planB, planC));

        assertEquals(3, determination.orderedPlans().size());
        assertTrue(determination.orderedPlans().get(0).isPrimary());
        assertEquals(2, determination.secondaryPlans().size());

        PaymentCalculation result = paymentCalculator.calculate(
                amount("1000"), amount("650"), amount("500"),
                new StandardPaymentStrategy());

        assertTrue(result.primaryPayment().add(result.secondaryPayment())
                .compareTo(amount("1000")) <= 0);
        assertEquals(amount("1000"),
                result.primaryPayment()
                        .add(result.secondaryPayment())
                        .add(result.patientResponsibility()));
    }

    @Test
    void nonDuplicationCanLeavePatientResponsibility() {
        PaymentCalculation result = paymentCalculator.calculate(
                amount("1000"), amount("800"), amount("700"),
                new NonDuplicationPaymentStrategy());

        assertEquals(amount("0"), result.secondaryPayment());
        assertEquals(amount("200"), result.patientResponsibility());
    }

    @Test
    void primaryDenialAllowsSecondaryBenefitToBeCalculated() {
        PaymentCalculation result = new DenialCoordinationService()
                .calculateAfterPrimaryDenial(
                        amount("1000"),
                        amount("700"),
                        new StandardPaymentStrategy(),
                        ClaimAdjudication.denied("Primary exclusion"));

        assertEquals(amount("0"), result.primaryPayment());
        assertEquals(amount("700"), result.secondaryPayment());
        assertEquals(amount("300"), result.patientResponsibility());
    }

    @Test
    void primaryChangesWhenEarlierBirthdayCoverageStartsMidYear() {
        Coverage oldCoverage = coverage(
                "OLD", parentA, "PLAN-A",
                CoverageRelationship.CHILD, CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2026, 1, 1), null, CobMethod.STANDARD);
        Coverage newCoverage = coverage(
                "NEW", parentB, "PLAN-B",
                CoverageRelationship.CHILD, CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2026, 7, 1), null, CobMethod.STANDARD);

        assertEquals("PLAN-A", determinationService.determine(
                claim(LocalDate.of(2026, 6, 30), "1000"),
                List.of(oldCoverage, newCoverage))
                .primaryPlan().coverage().plan().id());

        assertEquals("PLAN-B", determinationService.determine(
                claim(LocalDate.of(2026, 7, 1), "1000"),
                List.of(oldCoverage, newCoverage))
                .primaryPlan().coverage().plan().id());
    }

    @Test
    void primaryOnlyPaymentReconcilesWithPatientResponsibility() {
        PaymentCalculation result = paymentCalculator.calculatePrimaryOnly(
                amount("425.50"), amount("300.25"));

        assertEquals(amount("125.25"), result.patientResponsibility());
        assertEquals(amount("425.50"),
                result.primaryPayment().add(result.patientResponsibility()));
    }
}