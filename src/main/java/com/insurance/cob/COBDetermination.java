package com.insurance.cob;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record COBDetermination(
        Claim claim,
        List<PlanDetermination> orderedPlans) {

    public COBDetermination {
        Objects.requireNonNull(claim, "Claim is required");
        Objects.requireNonNull(orderedPlans, "Ordered plans are required");

        orderedPlans = List.copyOf(orderedPlans);

        if (orderedPlans.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one plan determination is required");
        }

        Set<String> coverageIds = new HashSet<>();

        for (int i = 0; i < orderedPlans.size(); i++) {
            PlanDetermination determination = orderedPlans.get(i);

            if (determination == null) {
                throw new IllegalArgumentException(
                        "Plan determination cannot be null");
            }

            if (determination.order() != i + 1) {
                throw new IllegalArgumentException(
                        "Plan orders must be sequential starting at 1");
            }

            Coverage coverage = determination.coverage();

            if (!coverage.patient().id().equals(claim.patient().id())) {
                throw new IllegalArgumentException(
                        "All coverages must belong to the claim patient");
            }

            if (!coverage.isActiveOn(claim.serviceDate())) {
                throw new IllegalArgumentException(
                        "Coverage must be active on the claim service date");
            }

            if (!coverageIds.add(coverage.id())) {
                throw new IllegalArgumentException(
                        "Duplicate coverage IDs are not allowed");
            }
        }
    }

    public PlanDetermination primaryPlan() {
        return orderedPlans.get(0);
    }

    public List<PlanDetermination> secondaryPlans() {
        return orderedPlans.subList(1, orderedPlans.size());
    }
}