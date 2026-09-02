package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BracketTournamentAlgorithmTest {

    private final BracketTournamentAlgorithm algorithm = new BracketTournamentAlgorithm();

    @Test
    void chooseWinnerIndex_HappyPath_ReturnsValidIndex() {
        List<Option> options = namedOptions("A", "B", "C");

        AlgorithmDetails algorithmDetails = algorithm.chooseWinnerIndex(options);
        int index = algorithmDetails.get("winnerIndex", Integer.class);

        assertTrue(index >= 0 && index < options.size());
    }

    @Test
    void chooseWinnerIndex_OptionsIsNull_ThrowsException() {
        assertThrows(NullPointerException.class, () -> algorithm.chooseWinnerIndex(null));
    }

    @Test
    void chooseWinnerIndex_OptionsWithOneElement_ThrowsException() {
        List<Option> options = List.of(mock(Option.class));
        assertThrows(Exceptions.InvalidRequestException.class, () -> algorithm.chooseWinnerIndex(options));
    }

    @Test
    @SuppressWarnings("unchecked")
    void chooseWinnerIndex_FourOptions_HasExactlyThreeDuelsAndNoByes() {
        // Con una cantidad de opciones potencia de 2 (4 = 2 rondas), nunca
        // debería haber "bye": todas las rondas emparejan a todos.
        List<Option> options = namedOptions("A", "B", "C", "D");

        AlgorithmDetails details = algorithm.chooseWinnerIndex(options);
        List<Map<String, Object>> steps = (List<Map<String, Object>>) details.get("steps", List.class);

        long duels = steps.stream().filter(s -> "narrative.bracket.duel".equals(s.get("descriptionKey"))).count();
        long byes = steps.stream().filter(s -> "narrative.bracket.bye".equals(s.get("descriptionKey"))).count();

        assertEquals(3, duels, "4 opciones -> 3 duelos para llegar a un ganador (N-1)");
        assertEquals(0, byes, "4 es potencia de 2, no debería haber byes");
        assertEquals("2", details.get("custom_rounds", String.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void chooseWinnerIndex_ThreeOptions_HasExactlyTwoDuelsAndOneBye() {
        List<Option> options = namedOptions("A", "B", "C");

        AlgorithmDetails details = algorithm.chooseWinnerIndex(options);
        List<Map<String, Object>> steps = (List<Map<String, Object>>) details.get("steps", List.class);

        long duels = steps.stream().filter(s -> "narrative.bracket.duel".equals(s.get("descriptionKey"))).count();
        long byes = steps.stream().filter(s -> "narrative.bracket.bye".equals(s.get("descriptionKey"))).count();

        assertEquals(2, duels, "3 opciones -> 2 duelos para llegar a un ganador (N-1)");
        assertEquals(1, byes, "3 es impar en la primera ronda -> exactamente un bye");
    }

    @Test
    void chooseWinnerIndex_FourOptions_OverManyTrials_DistributionIsApproximatelyUniform() {
        distributionIsApproximatelyUniform(namedOptions("A", "B", "C", "D"));
    }

    @Test
    void chooseWinnerIndex_ThreeOptions_WithByes_OverManyTrials_DistributionIsApproximatelyUniform() {
        // Caso más delicado: al haber un "bye", ¿el que lo recibe tiene
        // ventaja? No, porque quién recibe el bye en cada ronda también
        // queda determinado por el mezclado inicial (al azar) de todas las
        // opciones — así que sigue siendo uniforme entre las 3 opciones.
        distributionIsApproximatelyUniform(namedOptions("A", "B", "C"));
    }

    private void distributionIsApproximatelyUniform(List<Option> options) {
        int trials = 10_000;
        int[] wins = new int[options.size()];

        for (int i = 0; i < trials; i++) {
            int index = algorithm.chooseWinnerIndex(options).get("winnerIndex", Integer.class);
            wins[index]++;
        }

        double expected = 1.0 / options.size();
        for (int index = 0; index < wins.length; index++) {
            double rate = wins[index] / (double) trials;
            assertTrue(Math.abs(rate - expected) < 0.05,
                    "Índice " + index + " ganó " + rate + " de las veces, esperado ~" + expected + " (±0.05)");
        }
    }

    private List<Option> namedOptions(String... values) {
        return java.util.Arrays.stream(values)
                .map(v -> {
                    Option option = mock(Option.class);
                    lenient().when(option.getValue()).thenReturn(v);
                    return option;
                })
                .toList();
    }
}
