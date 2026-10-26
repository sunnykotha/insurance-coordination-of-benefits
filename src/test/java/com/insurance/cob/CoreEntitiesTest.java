package com.insurance.cob;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CoreEntitiesTest {

    private final LocalDate birthDate = LocalDate.of(1990, 5, 10);
    private final Patient patient =
            new Patient("P1", "Alex", birthDate);
    private final Policyholder policyholder =
            new Policyholder("P1", "Alex", birthDate);
    private final Plan plan =
            new Plan("PLAN1", "Health Plan", CobMethod.STANDARD);

    @Test
    void createsValidPatientAndPolicyholder() {
        assertEquals("P1", patient.id());
        assertEquals(birthDate, policyholder.dateOfBirth());
    }

    @Test
    void rejectsBlankPatientId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Patient(" ", "Alex", birthDate));
    }

    @Test
    void rejectsMissingPlanCobMethod() {
        assertThrows(IllegalArgumentException.class,
                () -> new Plan("PLAN1", "Health Plan", null));
    }

    @Test
    void createsValidSelfCoverage() {
        Coverage coverage = new Coverage(
                "C1", patient, policyholder, plan,
                CoverageRelationship.SELF,
                CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2026, 1, 1), null);

        assertTrue(coverage.isActiveOn(LocalDate.of(2026, 6, 1)));
    }

    @Test
    void coverageIsInactiveBeforeStartDate() {
        Coverage coverage = new Coverage(
                "C1", patient, policyholder, plan,
                CoverageRelationship.SELF,
                CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2026, 1, 1), null);

        assertFalse(coverage.isActiveOn(LocalDate.of(2025, 12, 31)));
    }

    @Test
    void coverageIsInactiveAfterEndDate() {
        Coverage coverage = new Coverage(
                "C1", patient, policyholder, plan,
                CoverageRelationship.SELF,
                CoverageType.ACTIVE_EMPLOYEE,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30));

        assertFalse(coverage.isActiveOn(LocalDate.of(2026, 7, 1)));
        assertTrue(coverage.isActiveOn(LocalDate.of(2026, 6, 30)));
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        assertThrows(IllegalArgumentException.class, () ->
                new Coverage(
                        "C1", patient, policyholder, plan,
                        CoverageRelationship.SELF,
                        CoverageType.ACTIVE_EMPLOYEE,
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 5, 31)));
    }

    @Test
    void createsValidClaim() {
        Claim claim = new Claim(
                "CL1", patient,
                LocalDate.of(2026, 6, 15),
                new BigDecimal("1000.00"));

        assertEquals(0,
                claim.allowedAmount().compareTo(new BigDecimal("1000.00")));
    }

    @Test
    void rejectsNegativeClaimAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                new Claim("CL1", patient,
                        LocalDate.of(2026, 6, 15),
                        new BigDecimal("-1.00")));
    }

    @Test
    void rejectsSelfCoverageForDifferentPatientAndPolicyholder() {
        Policyholder parent = new Policyholder(
                "P2", "Parent", LocalDate.of(1965, 3, 12));

        assertThrows(IllegalArgumentException.class, () ->
                new Coverage(
                        "C1", patient, parent, plan,
                        CoverageRelationship.SELF,
                        CoverageType.ACTIVE_EMPLOYEE,
                        LocalDate.of(2026, 1, 1), null));
    }
}