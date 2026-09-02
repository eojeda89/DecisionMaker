package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ThreadRaceAlgorithmTest {

    private final ThreadRaceAlgorithm algorithm = new ThreadRaceAlgorithm();

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
        // Nota: a diferencia de los demás algoritmos "de azar", acá no sumamos
        // un test de distribución con N grande (ej. 10.000 tiradas) — cada
        // tirada duerme threads reales 100-1000ms, así que 10.000 tiradas
        // tardarían minutos y volverían lenta toda la suite. 30 iteraciones
        // alcanza para cubrir la aleatoriedad del bounds-check.
        List<Option> options = List.of(mock(Option.class), mock(Option.class), mock(Option.class), mock(Option.class));

        for (int i = 0; i < 30; i++) {
            AlgorithmDetails algorithmDetails = algorithm.chooseWinnerIndex(options);
            int index = algorithmDetails.get("winnerIndex", Integer.class);
            assertTrue(index >= 0 && index < options.size(), "Índice fuera de rango: " + index);
        }
    }

    @Test
    void chooseWinnerIndex_Interrupted_ThrowsAlgorithmFailureException() throws Exception {
        ThreadRaceAlgorithm algo = new ThreadRaceAlgorithm() {
            @Override
            public AlgorithmDetails chooseWinnerIndex(List<Option> options) {
                Thread.currentThread().interrupt();
                return super.chooseWinnerIndex(options);
            }
        };
        List<Option> options = List.of(mock(Option.class), mock(Option.class));

        Exceptions.AlgorithmFailureException ex = assertThrows(
                Exceptions.AlgorithmFailureException.class,
                () -> algo.chooseWinnerIndex(options)
        );
        assertTrue(ex.getMessage().contains("Interrupted"));
    }

    @Test
    void chooseWinnerIndex_ExecutionException_ThrowsAlgorithmFailureException() {
        ThreadRaceAlgorithm algo = new ThreadRaceAlgorithm() {
            @Override
            public AlgorithmDetails chooseWinnerIndex(List<Option> options) {
                throw new CompletionException(new RuntimeException("Simulated error"));
            }
        };
        List<Option> options = List.of(mock(Option.class), mock(Option.class));

        assertThrows(CompletionException.class, () -> algo.chooseWinnerIndex(options));
    }
}