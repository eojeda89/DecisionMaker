package com.eojeda89.decididorapi.domain.model;

/**
 * Value Object para la identidad de Option.
 */
public record OptionId(Long value) {
    public OptionId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("OptionId must be positive if provided");
        }
    }

    public static OptionId of(Long value) {
        return new OptionId(value);
    }

    public boolean isAssigned() {
        return value != null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
