package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Resuelve a texto localizado las claves de mensaje que los algoritmos de
 * dominio devuelven en los campos "algorithm" y "description" de
 * {@link AlgorithmDetails} (ej. "algorithm.dice-roll.name"). Los algoritmos
 * en sí son clases puras sin dependencias de Spring; esta resolución vive
 * acá, en la capa de adaptador web, para no acoplar el dominio al framework.
 */
@Component
@RequiredArgsConstructor
public class AlgorithmDetailsLocalizer {

    private static final String ALGORITHM_KEY = "algorithm";
    private static final String DESCRIPTION_KEY = "description";

    private final MessageSource messageSource;

    public Map<String, Object> localize(AlgorithmDetails details) {
        if (details == null) return Map.of();
        Locale locale = LocaleContextHolder.getLocale();
        Map<String, Object> resolved = new LinkedHashMap<>(details.getProperties());
        resolveInPlace(resolved, ALGORITHM_KEY, locale);
        resolveInPlace(resolved, DESCRIPTION_KEY, locale);
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
}
