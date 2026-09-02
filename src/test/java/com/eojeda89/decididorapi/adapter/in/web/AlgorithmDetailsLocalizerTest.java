package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlgorithmDetailsLocalizerTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private AlgorithmDetailsLocalizer localizer;

    @Test
    void localize_ResolvesAlgorithmAndDescriptionKeys() {
        when(messageSource.getMessage(eq("algorithm.dice-roll.name"), any(), eq("algorithm.dice-roll.name"), any(Locale.class)))
                .thenReturn("Lanzamiento de dados");
        when(messageSource.getMessage(eq("algorithm.dice-roll.description"), any(), eq("algorithm.dice-roll.description"), any(Locale.class)))
                .thenReturn("Se elige un ganador al azar lanzando un dado.");

        AlgorithmDetails details = AlgorithmDetails.of(Map.of(
                "algorithm", "algorithm.dice-roll.name",
                "description", "algorithm.dice-roll.description",
                "custom_optionsCount", "3",
                "winnerIndex", 1
        ));

        Map<String, Object> resolved = localizer.localize(details);

        assertEquals("Lanzamiento de dados", resolved.get("algorithm"));
        assertEquals("Se elige un ganador al azar lanzando un dado.", resolved.get("description"));
        assertEquals("3", resolved.get("custom_optionsCount"));
        assertEquals(1, resolved.get("winnerIndex"));
    }

    @Test
    void localize_UnknownKey_FallsBackToKeyItself() {
        // Cubre decisiones persistidas antes de introducir claves de mensaje,
        // cuando "algorithm"/"description" todavía guardaban texto literal.
        when(messageSource.getMessage(eq("Lanzamiento de dados"), any(), eq("Lanzamiento de dados"), any(Locale.class)))
                .thenReturn("Lanzamiento de dados");

        AlgorithmDetails details = AlgorithmDetails.of(Map.of(
                "algorithm", "Lanzamiento de dados",
                "winnerIndex", 0
        ));

        Map<String, Object> resolved = localizer.localize(details);

        assertEquals("Lanzamiento de dados", resolved.get("algorithm"));
    }

    @Test
    void localize_NullDetails_ReturnsEmptyMap() {
        assertEquals(Map.of(), localizer.localize(null));
    }
}
