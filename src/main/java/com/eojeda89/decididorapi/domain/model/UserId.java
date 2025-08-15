package com.eojeda89.decididorapi.domain.model;

/**
 * Value Object para la identidad de Usuario.
 * Permite null para entidades transitorias (antes de persistir).
 */
public record UserId(Long value) {
    public UserId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("UserId must be positive if provided");
        }
    }

    public static UserId of(Long value) {
        return new UserId(value);
    }

    public boolean isAssigned() {
        return value != null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
