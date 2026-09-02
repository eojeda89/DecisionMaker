package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RandomWeightedAlgorithmTest {

    private final RandomWeightedAlgorithm algorithm = new RandomWeightedAlgorithm();

    @Test
    void chooseWinnerIndex_HappyPath_ReturnsValidIndex() {
        List<Option> options = List.of(mock(Option.class), mock(Option.class), mock(Option.class));

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
    void chooseWinnerIndex_AlwaysReturnsIndexWithinBounds() {
        List<Option> options = List.of(mock(Option.class), mock(Option.class), mock(Option.class), mock(Option.class));

        // Ejecutar varias veces para cubrir aleatoriedad
        for (int i = 0; i < 100; i++) {
            AlgorithmDetails algorithmDetails = algorithm.chooseWinnerIndex(options);
            int index = algorithmDetails.get("winnerIndex", Integer.class);
            assertTrue(index >= 0 && index < options.size(), "Índice fuera de rango: " + index);
        }
    }

    @Test
    void chooseWinnerIndex_HonorsRandomWeights_NotAlwaysLastOption() {
        // Regresión del bug: el bucle original nunca hacía `break` al encontrar
        // el ganador y una línea posterior forzaba winnerIndex = n - 1, por lo que
        // el "sorteo ponderado" terminaba eligiendo siempre la última opción sin
        // importar los pesos generados. Con el fix, la opción con mayor peso en
        // cada tirada debería ganar sensiblemente más seguido que 1/n (azar puro),
        // y el ganador no debería concentrarse siempre en el último índice.
        List<Option> options = List.of(mock(Option.class), mock(Option.class), mock(Option.class), mock(Option.class));
        int trials = 5000;
        int matchesWithHeaviestWeight = 0;
        int lastIndexWins = 0;

        for (int i = 0; i < trials; i++) {
            AlgorithmDetails details = algorithm.chooseWinnerIndex(options);
            int winnerIndex = details.get("winnerIndex", Integer.class);
            List<Integer> weights = parseWeights(details.get("custom_weights", String.class));

            int heaviestIndex = 0;
            for (int j = 1; j < weights.size(); j++) {
                if (weights.get(j) > weights.get(heaviestIndex)) heaviestIndex = j;
            }
            if (winnerIndex == heaviestIndex) matchesWithHeaviestWeight++;
            if (winnerIndex == options.size() - 1) lastIndexWins++;
        }

        double heaviestWinRate = matchesWithHeaviestWeight / (double) trials;
        double lastIndexWinRate = lastIndexWins / (double) trials;

        // Sin el bug, la opción de mayor peso gana ~40% de las veces (muy por
        // encima del 25% esperado por puro azar entre 4 opciones); con el bug,
        // el ganador era casi siempre el último índice y esta tasa rondaba el 25%.
        assertTrue(heaviestWinRate > 0.32,
                "La opción con mayor peso debería ganar sensiblemente más que al azar, tasa observada: " + heaviestWinRate);
        // Sin el bug, cada índice gana ~25% de las veces; con el bug, el último
        // índice ganaba prácticamente siempre (~100%).
        assertTrue(lastIndexWinRate < 0.40,
                "El último índice no debería concentrar casi todas las victorias, tasa observada: " + lastIndexWinRate);
    }

    private List<Integer> parseWeights(String weightsToString) {
        String trimmed = weightsToString.substring(1, weightsToString.length() - 1); // quita '[' y ']'
        return Arrays.stream(trimmed.split(",\\s*"))
                .map(Integer::parseInt)
                .toList();
    }
}
