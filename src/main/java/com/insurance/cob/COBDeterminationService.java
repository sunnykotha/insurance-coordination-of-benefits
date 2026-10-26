package com.insurance.cob;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class COBDeterminationService {

    public COBDetermination determine(Claim claim, List<Coverage> coverages) {
        return determine(claim, coverages, null);
    }

    public COBDetermination determine(
            Claim claim,
            List<Coverage> coverages,
            CourtOrder courtOrder) {

        Objects.requireNonNull(claim, "Claim is required");
        Objects.requireNonNull(coverages, "Coverages are required");

        if (coverages.isEmpty())
            throw new IllegalArgumentException("At least one coverage is required");

        Set<String> ids = new HashSet<>();

        for (Coverage coverage : coverages) {
            Objects.requireNonNull(coverage, "Coverage cannot be null");

            if (!coverage.patient().id().equals(claim.patient().id()))
                throw new IllegalArgumentException(
                        "All coverages must belong to the claim patient");

            if (!ids.add(coverage.id()))
                throw new IllegalArgumentException("Duplicate coverage IDs are not allowed");
        }

        if (courtOrder != null
                && !courtOrder.patientId().equals(claim.patient().id())) {
            throw new IllegalArgumentException(
                    "Court order must belong to the claim patient");
        }

        List<Coverage> active = coverages.stream()
                .filter(c -> c.isActiveOn(claim.serviceDate()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        if (active.isEmpty())
            throw new IllegalArgumentException(
                    "No coverage is active on the claim service date");

        active.sort(comparator(claim, courtOrder));

        Coverage primary = active.get(0);

        List<PlanDetermination> ordered = new ArrayList<>();

        for (int i = 0; i < active.size(); i++) {
            Coverage coverage = active.get(i);

            ordered.add(new PlanDetermination(
                    coverage,
                    i + 1,
                    reasonFor(coverage, primary, claim, courtOrder)));
        }

        return new COBDetermination(claim, ordered);
    }

    private Comparator<Coverage> comparator(Claim claim, CourtOrder courtOrder) {
        LocalDate serviceDate = claim.serviceDate();

        return Comparator
                // Subscriber coverage precedes dependent coverage.
                .comparingInt((Coverage c) ->
                        c.relationship() == CoverageRelationship.SELF ? 0 : 1)

                // Active-employee coverage precedes COBRA and retiree coverage.
                .thenComparingInt(c -> employmentRank(c.coverageType()))

                // An applicable court order takes precedence over the birthday rule.
                .thenComparingInt(c -> courtOrderRank(c, claim, courtOrder))

                // Birthday rule applies when both coverages are for the child.
                .thenComparing(c -> birthdayKey(c, claim, courtOrder))

                // Earlier coverage start date wins remaining ties.
                .thenComparing(Coverage::startDate)

                // Stable final tie-breaker.
                .thenComparing(Coverage::id);
    }

    private int employmentRank(CoverageType type) {
        return switch (type) {
            case COBRA, RETIREE -> 1;
            case ACTIVE_EMPLOYEE, OTHER -> 0;
        };
    }

    private int courtOrderRank(
            Coverage coverage, Claim claim, CourtOrder courtOrder) {

        if (courtOrder == null
                || coverage.relationship() != CoverageRelationship.CHILD
                || !courtOrder.appliesTo(claim.patient(), claim.serviceDate())) {
            return 1;
        }

        return coverage.policyholder().id()
                .equals(courtOrder.primaryPolicyholderId()) ? 0 : 1;
    }

    private MonthDay birthdayKey(
            Coverage coverage, Claim claim, CourtOrder courtOrder) {

        boolean courtOrderApplies = courtOrder != null
                && courtOrder.appliesTo(claim.patient(), claim.serviceDate());

        if (coverage.relationship() == CoverageRelationship.CHILD
                && !courtOrderApplies) {
            return MonthDay.from(coverage.policyholder().dateOfBirth());
        }

        // When birthday comparison is not applicable, all coverages share
        // the same key and proceed to the next comparator.
        return MonthDay.of(1, 1);
    }

    private String reasonFor(
            Coverage coverage,
            Coverage primary,
            Claim claim,
            CourtOrder courtOrder) {

        if (coverage.id().equals(primary.id())) {
            if (coverage.relationship() == CoverageRelationship.SELF)
                return "Primary: patient is the subscriber";

            if (courtOrder != null
                    && courtOrder.appliesTo(claim.patient(), claim.serviceDate())
                    && coverage.relationship() == CoverageRelationship.CHILD
                    && coverage.policyholder().id()
                        .equals(courtOrder.primaryPolicyholderId())) {
                return "Primary: applicable court order";
            }

            if (employmentRank(coverage.coverageType())
                    < employmentRankForOtherActiveCoverages(claim, coverage)) {
                return "Primary: active-employee coverage takes precedence";
            }

            if (coverage.relationship() == CoverageRelationship.CHILD)
                return "Primary: birthday rule or longer child coverage";

            return "Primary: applicable coordination-of-benefits ordering rules";
        }

        if (coverage.relationship() == CoverageRelationship.SELF)
            return "Secondary: another plan has priority under subscriber rules";

        if (courtOrder != null
                && courtOrder.appliesTo(claim.patient(), claim.serviceDate())
                && coverage.relationship() == CoverageRelationship.CHILD
                && coverage.policyholder().id()
                    .equals(courtOrder.primaryPolicyholderId())) {
            return "Ordered under court order; another applicable rule ranks first";
        }

        if (coverage.relationship() == CoverageRelationship.CHILD)
            return "Ordered by employment status, birthday rule, coverage duration, and tie-breaker";

        return "Ordered by employment status and deterministic tie-breaker";
    }

    private int employmentRankForOtherActiveCoverages(
            Claim claim, Coverage selected) {

        return selected.coverageType() == CoverageType.ACTIVE_EMPLOYEE ? 1 : 0;
    }
}