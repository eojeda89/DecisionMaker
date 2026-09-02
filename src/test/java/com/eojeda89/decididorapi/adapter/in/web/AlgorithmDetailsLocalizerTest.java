package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    @SuppressWarnings("unchecked")
    void localize_ResolvesStepsWithPositionalArgs() {
        when(messageSource.getMessage(
                eq("narrative.bracket.duel"), eq(new Object[]{1, "Pizza", "Sushi", "Pizza"}), eq("narrative.bracket.duel"), any(Locale.class)
        )).thenReturn("Ronda 1: Pizza vs. Sushi → ganó Pizza (cara o cruz).");

        AlgorithmDetails details = AlgorithmDetails.of(Map.of(
                "winnerIndex", 0,
                "steps", List.of(Map.of(
                        "round", 1,
                        "descriptionKey", "narrative.bracket.duel",
                        "args", List.of(1, "Pizza", "Sushi", "Pizza")
                ))
        ));

        Map<String, Object> resolved = localizer.localize(details);
        List<Map<String, Object>> steps = (List<Map<String, Object>>) resolved.get("steps");

        assertEquals(1, steps.size());
        assertEquals(1, steps.get(0).get("round"));
        assertEquals("Ronda 1: Pizza vs. Sushi → ganó Pizza (cara o cruz).", steps.get(0).get("text"));
    }

    @Test
    void localize_NoStepsKey_DoesNotAddStepsToResult() {
        AlgorithmDetails details = AlgorithmDetails.of(Map.of("winnerIndex", 0));

        Map<String, Object> resolved = localizer.localize(details);

        assertFalse(resolved.containsKey("steps"));
    }
}
