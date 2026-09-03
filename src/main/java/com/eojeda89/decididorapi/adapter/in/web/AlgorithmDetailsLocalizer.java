package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Resuelve a texto localizado las claves de mensaje que los algoritmos de
 * dominio devuelven en los campos "algorithm" y "description" de
 * {@link AlgorithmDetails} (ej. "algorithm.dice-roll.name"), y en cada paso
 * de la narrativa "steps" (Fase 3.7: {round, descriptionKey, args}, tipo
 * MessageFormat). Los algoritmos en sí son clases puras sin dependencias de
 * Spring; esta resolución vive acá, en la capa de adaptador web, para no
 * acoplar el dominio al framework.
 */
@Component
@RequiredArgsConstructor
public class AlgorithmDetailsLocalizer {

    private static final String ALGORITHM_KEY = "algorithm";
    private static final String DESCRIPTION_KEY = "description";
    private static final String STEPS_KEY = "steps";

    // BestOfNDecisionService pasa el AlgorithmType.code (no el uiName, que
    // está hardcodeado en español) como arg posicional en estas dos claves,
    // precisamente para que quede localizable acá en vez de resuelto de
    // antemano en la capa de aplicación. El índice indica en qué posición
    // de "args" viene ese código.
    private static final Map<String, Integer> ALGORITHM_ARG_INDEX = Map.of(
            "narrative.best-of-n.round", 1,
            "narrative.best-of-n.tiebreak", 0
    );

    private final MessageSource messageSource;

    public Map<String, Object> localize(AlgorithmDetails details) {
        if (details == null) return Map.of();
        Locale locale = LocaleContextHolder.getLocale();
        Map<String, Object> resolved = new LinkedHashMap<>(details.getProperties());
        resolveInPlace(resolved, ALGORITHM_KEY, locale);
        resolveInPlace(resolved, DESCRIPTION_KEY, locale);
        resolveSteps(resolved, locale);
        return resolved;
    }

    // Si la clave no existe en messages.properties, se devuelve tal cual: así
    // las decisiones ya persistidas con texto literal (antes de introducir
    // claves de mensaje) se siguen mostrando igual que siempre, sin romper.
    private void resolveInPlace(Map<String, Object> details, String key, Locale locale) {
        if (details.get(key) instanceof String code) {
            details.put(key, messageSource.getMessage(code, null, code, locale));
        }
    }

    @SuppressWarnings("unchecked")
    private void resolveSteps(Map<String, Object> details, Locale locale) {
        if (!(details.get(STEPS_KEY) instanceof List<?> rawSteps)) return;

        List<Map<String, Object>> resolvedSteps = new ArrayList<>();
        for (Object rawStep : rawSteps) {
            if (!(rawStep instanceof Map<?, ?> stepMap)) continue;
            Map<String, Object> resolvedStep = new LinkedHashMap<>();
            Object round = stepMap.get("round");
            if (round != null) resolvedStep.put("round", round);

            Object descriptionKey = stepMap.get("descriptionKey");
            if (descriptionKey instanceof String key) {
                Object argsValue = stepMap.get("args");
                Object[] args = argsValue instanceof List<?> argList ? argList.toArray() : new Object[0];
                args = resolveAlgorithmArg(key, args, locale);
                resolvedStep.put("text", messageSource.getMessage(key, args, key, locale));
            }
            resolvedSteps.add(resolvedStep);
        }
        details.put(STEPS_KEY, resolvedSteps);
    }

    private Object[] resolveAlgorithmArg(String descriptionKey, Object[] args, Locale locale) {
        Integer index = ALGORITHM_ARG_INDEX.get(descriptionKey);
        if (index == null || index >= args.length || !(args[index] instanceof String algorithmCode)) {
            return args;
        }
        Object[] resolved = args.clone();
        String nameKey = "algorithm." + algorithmCode + ".name";
        resolved[index] = messageSource.getMessage(nameKey, null, algorithmCode, locale);
        return resolved;
    }
}
