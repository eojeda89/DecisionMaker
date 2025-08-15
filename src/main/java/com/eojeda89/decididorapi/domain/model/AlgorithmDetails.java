package com.eojeda89.decididorapi.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Value Object para encapsular los detalles de configuración del algoritmo.
 *
 * Inmutable y con acceso tipado. Puedes extender validate() para
 * reglas específicas según AlgorithmType en capas superiores si lo necesitas.
 */
@Getter
@EqualsAndHashCode
public final class AlgorithmDetails {
    private final Map<String, Object> properties;

    private AlgorithmDetails(Map<String, Object> properties) {
        this.properties = properties == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(properties);
        validate();
    }

    public static AlgorithmDetails of(Map<String, Object> properties) {
        return new AlgorithmDetails(properties);
    }

    public <T> T get(String key, Class<T> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        Object value = properties.get(key);
        if (value == null) return null;
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Expected type " + type.getSimpleName() + " for key '" + key + "' but was " + value.getClass().getSimpleName()
            );
        }
        return type.cast(value);
    }

    private void validate() {
        // TODO: Agrega validaciones de claves/valores requeridos si aplica.
    }
}
