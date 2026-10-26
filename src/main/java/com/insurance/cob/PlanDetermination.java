package com.insurance.cob;

public record PlanDetermination(
        Coverage coverage,
        int order,
        String reason) {

    public PlanDetermination {
        if (coverage == null) {
            throw new IllegalArgumentException("Coverage is required");
        }
        if (order < 1) {
            throw new IllegalArgumentException("Plan order must be at least 1");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Determination reason is required");
        }
    }

    public boolean isPrimary() {
        return order == 1;
    }
}