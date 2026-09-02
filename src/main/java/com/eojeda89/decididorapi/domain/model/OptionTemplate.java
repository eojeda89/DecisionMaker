package com.eojeda89.decididorapi.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Set de opciones reutilizable (Fase 3.6), ej. "restaurantes del barrio",
 * para no tener que volver a tipear la misma lista en cada decisión. No
 * decide nada por sí solo -- solo guarda una lista de valores para que el
 * cliente los reuse al armar una decisión normal.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OptionTemplate {
    private TemplateId id;
    private UserId userId;
    private String name;
    private List<String> optionValues;
    private Instant createdAt;
}
