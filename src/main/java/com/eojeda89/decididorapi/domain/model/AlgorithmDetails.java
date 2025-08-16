package com.eojeda89.decididorapi.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

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
        if (properties == null) {
            throw new IllegalArgumentException("Properties map cannot be null");
        }
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isEmpty()) {
                throw new IllegalArgumentException("Property key cannot be null or empty");
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException("Property value for key '" + entry.getKey() + "' cannot be null");
            }
        }
    }
}
