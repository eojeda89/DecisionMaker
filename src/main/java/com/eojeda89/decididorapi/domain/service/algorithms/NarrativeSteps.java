package com.eojeda89.decididorapi.domain.service.algorithms;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Helper para construir la lista de "steps" (narrativa paso a paso, Fase 3.7)
 * que cada algoritmo agrega a su {@link com.eojeda89.decididorapi.domain.model.AlgorithmDetails}.
 * Cada step es {round, descriptionKey, args} — una clave de mensaje tipo
 * MessageFormat más sus argumentos posicionales, resuelta a texto por
 * adapter/in/web/AlgorithmDetailsLocalizer (el dominio no conoce Spring ni
 * MessageSource, solo produce las claves).
 */
final class NarrativeSteps {

    private NarrativeSteps() {
    }

    static List<Map<String, Object>> singleStep(String descriptionKey, Object... args) {
        // Arrays.asList (no List.of): permite null en args. En producción no
        // debería pasar (Option.getValue() está validado en el borde de la
        // API), pero los tests existentes usan mocks de Option sin stubear
        // getValue() con frecuencia, y List.of() rechaza null con NPE.
        return List.of(Map.of(
                "round", 1,
                "descriptionKey", descriptionKey,
                "args", Arrays.asList(args)
        ));
    }
}
