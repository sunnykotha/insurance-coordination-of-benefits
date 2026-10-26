package com.insurance.cob;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoverageChangeTest {

    private final COBDeterminationService service =
            new COBDeterminationService();

    private final Patient child = new Patient(
            "CHILD", "Child", LocalDate.of(2015, 6, 10));

    private final Policyholder parentA = new Policyholder(
            "PA", "Parent A", LocalDate.of(1980, 3, 10));

    private final Policyholder parentB = new Policyholder(
            "PB", "Parent B", LocalDate.of(1978, 2, 20));

    private Coverage coverage(
            String id, Policyholder holder, String planId,
            LocalDate start, LocalDate end) {

        return new Coverage(
                id, child, holder,
                new Plan(planId, planId, CobMethod.STANDARD),
                CoverageRelationship.CHILD,
                CoverageType.ACTIVE_EMPLOYEE,
                start, end);
    }

    private Claim claim(LocalDate date) {
        return new Claim(
                "CL-" + date,
                child,
                date,
                new BigDecimal("1000.00"));
    }

    @Test
    void primaryChangesWhenNewCoverageBecomesActive() {
        Coverage oldPlan = coverage(
                "OLD", parentA, "PLAN-A",
                LocalDate.of(2026, 1, 1), null);

        Coverage newPlan = coverage(
                "NEW", parentB, "PLAN-B",
                LocalDate.of(2026, 7, 1), null);

        COBDetermination before = service.determine(
                claim(LocalDate.of(2026, 6, 30)),
                List.of(oldPlan, newPlan));

        COBDetermination after = service.determine(
                claim(LocalDate.of(2026, 7, 1)),
                List.of(oldPlan, newPlan));

        assertEquals("PLAN-A",
                before.primaryPlan().coverage().plan().id());

        assertEquals("PLAN-B",
                after.primaryPlan().coverage().plan().id());
    }

    @Test
    void coverageEndingOnServiceDateIsStillActive() {
        Coverage ending = coverage(
                "END", parentA, "PLAN-A",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30));

        COBDetermination result = service.determine(
                claim(LocalDate.of(2026, 6, 30)),
                List.of(ending));

        assertEquals(1, result.orderedPlans().size());
    }

    @Test
    void coverageEndingBeforeServiceDateIsExcluded() {
        Coverage ending = coverage(
                "END", parentA, "PLAN-A",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 29));

        Coverage current = coverage(
                "CURRENT", parentB, "PLAN-B",
                LocalDate.of(2026, 6, 30), null);

        COBDetermination result = service.determine(
                claim(LocalDate.of(2026, 6, 30)),
                List.of(ending, current));

        assertEquals(1, result.orderedPlans().size());
        assertEquals("PLAN-B",
                result.primaryPlan().coverage().plan().id());
    }

    @Test
    void orderingDoesNotDependOnInputListOrder() {
        Coverage a = coverage(
                "A", parentA, "PLAN-A",
                LocalDate.of(2026, 1, 1), null);

        Coverage b = coverage(
                "B", parentB, "PLAN-B",
                LocalDate.of(2026, 1, 1), null);

        COBDetermination first = service.determine(
                claim(LocalDate.of(2026, 6, 15)),
                List.of(a, b));

        COBDetermination second = service.determine(
                claim(LocalDate.of(2026, 6, 15)),
                List.of(b, a));

        assertEquals(
                first.orderedPlans().stream()
                        .map(p -> p.coverage().id()).toList(),
                second.orderedPlans().stream()
                        .map(p -> p.coverage().id()).toList());
    }

    @Test
    void inactiveThirdPlanIsExcludedFromThreePlanResult() {
        Coverage a = coverage(
                "A", parentA, "PLAN-A",
                LocalDate.of(2026, 1, 1), null);

        Coverage b = coverage(
                "B", parentB, "PLAN-B",
                LocalDate.of(2026, 1, 1), null);

        Policyholder parentC = new Policyholder(
                "PC", "Parent C", LocalDate.of(1985, 11, 5));

        Coverage c = coverage(
                "C", parentC, "PLAN-C",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 14));

        COBDetermination result = service.determine(
                claim(LocalDate.of(2026, 6, 15)),
                List.of(c, b, a));

        assertEquals(2, result.orderedPlans().size());
        assertTrue(result.orderedPlans().stream()
                .noneMatch(p -> p.coverage().id().equals("C")));
    }
}