package com.insurance.cob;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class COBDeterminationTest {

    private final LocalDate serviceDate = LocalDate.of(2026, 6, 15);

    private final Patient patient = new Patient(
            "P1", "Alex", LocalDate.of(1990, 5, 10));

    private final Policyholder subscriber = new Policyholder(
            "P1", "Alex", LocalDate.of(1990, 5, 10));

    private final Plan plan1 = new Plan(
            "PLAN1", "Plan One", CobMethod.STANDARD);

    private final Plan plan2 = new Plan(
            "PLAN2", "Plan Two", CobMethod.NON_DUPLICATION);

    private final Plan plan3 = new Plan(
            "PLAN3", "Plan Three", CobMethod.CARVE_OUT);

    private final Claim claim = new Claim(
            "CL1", patient, serviceDate, new BigDecimal("1000.00"));

    private Coverage coverage(String id, Plan plan) {
        return new Coverage(
                id, patient, subscriber, plan,
                CoverageRelationship.SELF,
                CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2026, 1, 1), null);
    }

    @Test
    void identifiesPrimaryPlan() {
        PlanDetermination primary = new PlanDetermination(
                coverage("C1", plan1), 1, "Subscriber coverage");

        COBDetermination result = new COBDetermination(
                claim, List.of(primary));

        assertTrue(result.primaryPlan().isPrimary());
        assertEquals("PLAN1", result.primaryPlan().coverage().plan().id());
    }

    @Test
    void identifiesSecondaryPlans() {
        COBDetermination result = new COBDetermination(claim, List.of(
                new PlanDetermination(coverage("C1", plan1), 1, "Primary"),
                new PlanDetermination(coverage("C2", plan2), 2, "Secondary"),
                new PlanDetermination(coverage("C3", plan3), 3, "Tertiary")
        ));

        assertEquals(2, result.secondaryPlans().size());
        assertEquals(3, result.orderedPlans().size());
        assertEquals(2, result.secondaryPlans().get(0).order());
        assertEquals(3, result.secondaryPlans().get(1).order());
    }

    @Test
    void preservesDeterminationReasons() {
        PlanDetermination determination = new PlanDetermination(
                coverage("C1", plan1), 1,
                "Patient is the subscriber");

        assertEquals("Patient is the subscriber", determination.reason());
    }

    @Test
    void rejectsInvalidPlanOrder() {
        assertThrows(IllegalArgumentException.class, () ->
                new PlanDetermination(
                        coverage("C1", plan1), 0, "Invalid order"));
    }

    @Test
    void rejectsBlankReason() {
        assertThrows(IllegalArgumentException.class, () ->
                new PlanDetermination(
                        coverage("C1", plan1), 1, " "));
    }

    @Test
    void rejectsEmptyDetermination() {
        assertThrows(IllegalArgumentException.class, () ->
                new COBDetermination(claim, List.of()));
    }

    @Test
    void rejectsNonSequentialPlanOrders() {
        assertThrows(IllegalArgumentException.class, () ->
                new COBDetermination(claim, List.of(
                        new PlanDetermination(
                                coverage("C1", plan1), 1, "Primary"),
                        new PlanDetermination(
                                coverage("C2", plan2), 3, "Secondary")
                )));
    }

    @Test
    void rejectsDuplicateCoverageIds() {
        assertThrows(IllegalArgumentException.class, () ->
                new COBDetermination(claim, List.of(
                        new PlanDetermination(
                                coverage("C1", plan1), 1, "Primary"),
                        new PlanDetermination(
                                coverage("C1", plan2), 2, "Secondary")
                )));
    }

    @Test
    void rejectsCoverageForAnotherPatient() {
        Patient otherPatient = new Patient(
                "P2", "Jordan", LocalDate.of(1985, 4, 20));

        Coverage otherCoverage = new Coverage(
                "C2", otherPatient,
                new Policyholder(
                        "P2", "Jordan", LocalDate.of(1985, 4, 20)),
                plan2, CoverageRelationship.SELF,
                CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2026, 1, 1), null);

        assertThrows(IllegalArgumentException.class, () ->
                new COBDetermination(claim, List.of(
                        new PlanDetermination(
                                coverage("C1", plan1), 1, "Primary"),
                        new PlanDetermination(
                                otherCoverage, 2, "Secondary")
                )));
    }

    @Test
    void rejectsCoverageInactiveOnServiceDate() {
        Coverage expired = new Coverage(
                "C4", patient, subscriber, plan2,
                CoverageRelationship.SELF,
                CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2026, 6, 14));

        assertThrows(IllegalArgumentException.class, () ->
                new COBDetermination(claim, List.of(
                        new PlanDetermination(
                                coverage("C1", plan1), 1, "Primary"),
                        new PlanDetermination(
                                expired, 2, "Expired coverage")
                )));
    }

    @Test
    void determinationListIsImmutable() {
        List<PlanDetermination> plans = new java.util.ArrayList<>();
        plans.add(new PlanDetermination(
                coverage("C1", plan1), 1, "Primary"));

        COBDetermination result = new COBDetermination(claim, plans);
        plans.clear();

        assertEquals(1, result.orderedPlans().size());
        assertThrows(UnsupportedOperationException.class,
                () -> result.orderedPlans().clear());
    }
}