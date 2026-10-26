package com.insurance.cob;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class COBDeterminationServiceTest {

    private final COBDeterminationService service = new COBDeterminationService();

    private final Patient child = new Patient(
            "CHILD", "Child", LocalDate.of(2015, 6, 10));

    private final Claim claim = new Claim(
            "CL1", child, LocalDate.of(2026, 6, 15),
            new BigDecimal("1000.00"));

    private final Policyholder parentA = new Policyholder(
            "PA", "Parent A", LocalDate.of(1980, 3, 10));

    private final Policyholder parentB = new Policyholder(
            "PB", "Parent B", LocalDate.of(1978, 8, 20));

    private Coverage childCoverage(
            String id, Policyholder parent, String planId,
            CoverageType type, LocalDate start) {

        return new Coverage(
                id, child, parent,
                new Plan(planId, planId, CobMethod.STANDARD),
                CoverageRelationship.CHILD, type, start, null);
    }

    @Test
    void earlierBirthdayMakesParentPrimary() {
        Coverage a = childCoverage("CA", parentA, "A",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2020, 1, 1));
        Coverage b = childCoverage("CB", parentB, "B",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2021, 1, 1));

        COBDetermination result = service.determine(claim, List.of(b, a));

        assertEquals("A", result.primaryPlan().coverage().plan().id());
        assertTrue(result.primaryPlan().reason().contains("Primary"));
    }

    @Test
    void birthdayRuleUsesMonthAndDayNotBirthYear() {
        Policyholder olderYearLaterBirthday = new Policyholder(
                "PC", "Parent C", LocalDate.of(1960, 12, 1));

        Coverage a = childCoverage("CA", parentA, "A",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2020, 1, 1));
        Coverage c = childCoverage("CC", olderYearLaterBirthday, "C",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2019, 1, 1));

        COBDetermination result = service.determine(claim, List.of(c, a));

        assertEquals("A", result.primaryPlan().coverage().plan().id());
    }

    @Test
    void sameBirthdayUsesLongerChildCoverage() {
        Policyholder sameBirthday = new Policyholder(
                "PB", "Parent B", LocalDate.of(1975, 3, 10));

        Coverage newer = childCoverage("CB", sameBirthday, "B",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2022, 1, 1));
        Coverage older = childCoverage("CA", parentA, "A",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2020, 1, 1));

        COBDetermination result = service.determine(claim, List.of(newer, older));

        assertEquals("A", result.primaryPlan().coverage().plan().id());
    }

    @Test
    void courtOrderOverridesBirthdayRule() {
        Coverage birthdayWinner = childCoverage("CA", parentA, "A",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2020, 1, 1));
        Coverage courtWinner = childCoverage("CB", parentB, "B",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2021, 1, 1));

        CourtOrder order = new CourtOrder(
                "CHILD", "PB",
                LocalDate.of(2025, 1, 1), null);

        COBDetermination result = service.determine(
                claim, List.of(birthdayWinner, courtWinner), order);

        assertEquals("B", result.primaryPlan().coverage().plan().id());
        assertTrue(result.primaryPlan().reason().contains("court order"));
    }

    @Test
    void expiredCourtOrderDoesNotOverrideBirthdayRule() {
        Coverage a = childCoverage("CA", parentA, "A",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2020, 1, 1));
        Coverage b = childCoverage("CB", parentB, "B",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2021, 1, 1));

        CourtOrder order = new CourtOrder(
                "CHILD", "PB",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2025, 12, 31));

        COBDetermination result = service.determine(claim, List.of(b, a), order);

        assertEquals("A", result.primaryPlan().coverage().plan().id());
    }

    @Test
    void activeEmployeeCoveragePrecedesCobraCoverage() {
        Coverage cobra = childCoverage("CA", parentA, "A",
                CoverageType.COBRA, LocalDate.of(2020, 1, 1));
        Coverage active = childCoverage("CB", parentB, "B",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2021, 1, 1));

        COBDetermination result = service.determine(claim, List.of(cobra, active));

        assertEquals("B", result.primaryPlan().coverage().plan().id());
    }

    @Test
    void activeEmployeeCoveragePrecedesRetireeCoverage() {
        Coverage retiree = childCoverage("CA", parentA, "A",
                CoverageType.RETIREE, LocalDate.of(2020, 1, 1));
        Coverage active = childCoverage("CB", parentB, "B",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2021, 1, 1));

        COBDetermination result = service.determine(claim, List.of(retiree, active));

        assertEquals("B", result.primaryPlan().coverage().plan().id());
    }

    @Test
    void excludesCoverageNotActiveOnServiceDate() {
        Coverage expired = new Coverage(
                "OLD", child, parentA,
                new Plan("OLDPLAN", "Old Plan", CobMethod.STANDARD),
                CoverageRelationship.CHILD, CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2020, 1, 1), LocalDate.of(2026, 6, 14));

        Coverage active = childCoverage("NEW", parentB, "NEWPLAN",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2026, 6, 15));

        COBDetermination result = service.determine(claim, List.of(expired, active));

        assertEquals(1, result.orderedPlans().size());
        assertEquals("NEWPLAN", result.primaryPlan().coverage().plan().id());
    }

    @Test
    void supportsThreeOrMorePlans() {
        Policyholder parentC = new Policyholder(
                "PC", "Parent C", LocalDate.of(1985, 11, 5));

        Coverage a = childCoverage("CA", parentA, "A",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2020, 1, 1));
        Coverage b = childCoverage("CB", parentB, "B",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2020, 1, 1));
        Coverage c = childCoverage("CC", parentC, "C",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2020, 1, 1));

        COBDetermination result = service.determine(claim, List.of(c, b, a));

        assertEquals(3, result.orderedPlans().size());
        assertEquals(1, result.primaryPlan().order());
        assertEquals(2, result.secondaryPlans().get(0).order());
        assertEquals(3, result.secondaryPlans().get(1).order());
    }

    @Test
    void rejectsWhenNoCoverageIsActive() {
        Coverage expired = new Coverage(
                "OLD", child, parentA,
                new Plan("OLDPLAN", "Old Plan", CobMethod.STANDARD),
                CoverageRelationship.CHILD, CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2020, 1, 1), LocalDate.of(2025, 12, 31));

        assertThrows(IllegalArgumentException.class,
                () -> service.determine(claim, List.of(expired)));
    }

    @Test
    void rejectsCoverageForAnotherPatient() {
        Patient other = new Patient(
                "OTHER", "Other", LocalDate.of(2014, 1, 1));

        Coverage wrong = new Coverage(
                "WRONG", other,
                new Policyholder("OTHER", "Other", LocalDate.of(2014, 1, 1)),
                new Plan("X", "X", CobMethod.STANDARD),
                CoverageRelationship.SELF, CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2020, 1, 1), null);

        assertThrows(IllegalArgumentException.class,
                () -> service.determine(claim, List.of(wrong)));
    }

    @Test
    void rejectsCourtOrderForAnotherPatient() {
        Coverage a = childCoverage("CA", parentA, "A",
                CoverageType.ACTIVE_EMPLOYEE, LocalDate.of(2020, 1, 1));

        CourtOrder wrongOrder = new CourtOrder(
                "OTHER", "PB", LocalDate.of(2020, 1, 1), null);

        assertThrows(IllegalArgumentException.class,
                () -> service.determine(claim, List.of(a), wrongOrder));
    }
}