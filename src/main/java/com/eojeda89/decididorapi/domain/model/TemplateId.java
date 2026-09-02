package com.eojeda89.decididorapi.domain.model;

/**
 * Value Object para la identidad de OptionTemplate (Fase 3.6).
 */
public record TemplateId(Long value) {
    public TemplateId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("TemplateId must be positive if provided");
        }
    }

    public static TemplateId of(Long value) {
        return new TemplateId(value);
    }

    public boolean isAssigned() {
        return value != null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
