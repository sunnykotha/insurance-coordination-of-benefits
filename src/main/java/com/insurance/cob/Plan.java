package com.insurance.cob;

public record Plan(String id, String name, CobMethod cobMethod) {
    public Plan {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Plan ID is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Plan name is required");
        if (cobMethod == null) throw new IllegalArgumentException("COB method is required");
    }
}