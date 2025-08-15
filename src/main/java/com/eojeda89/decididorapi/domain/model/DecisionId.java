package com.eojeda89.decididorapi.domain.model;

/**
 * Value Object para la identidad de Decision.
 */
public record DecisionId(Long value) {
    public DecisionId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("DecisionId must be positive if provided");
        }
    }

    public static DecisionId of(Long value) {
        return new DecisionId(value);
    }

    public boolean isAssigned() {
        return value != null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
