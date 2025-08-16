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
        AlgorithmDetails details = AlgorithmDetails.of(null);

        int index = algorithm.chooseWinnerIndex(options, details);

        assertTrue(index >= 0 && index < options.size());
    }

    @Test
    void chooseWinnerIndex_OptionsIsNull_ThrowsException() {
        AlgorithmDetails details = AlgorithmDetails.of(null);
        assertThrows(NullPointerException.class, () -> algorithm.chooseWinnerIndex(null, details));
    }

    @Test
    void chooseWinnerIndex_OptionsWithOneElement_ThrowsException() {
        List<Option> options = List.of(mock(Option.class));
        assertThrows(Exceptions.InvalidRequestException.class, () -> algorithm.chooseWinnerIndex(options, AlgorithmDetails.of(null)));
    }

    @Test
    void chooseWinnerIndex_AlwaysReturnsIndexWithinBounds() {
        List<Option> options = List.of(mock(Option.class), mock(Option.class), mock(Option.class), mock(Option.class));
        AlgorithmDetails details = AlgorithmDetails.of(null);

        for (int i = 0; i < 30; i++) {
            int index = algorithm.chooseWinnerIndex(options, details);
            assertTrue(index >= 0 && index < options.size(), "Índice fuera de rango: " + index);
        }
    }

    @Test
    void chooseWinnerIndex_Interrupted_ThrowsAlgorithmFailureException() throws Exception {
        ThreadRaceAlgorithm algo = new ThreadRaceAlgorithm() {
            @Override
            public int chooseWinnerIndex(List<Option> options, AlgorithmDetails details) {
                Thread.currentThread().interrupt();
                return super.chooseWinnerIndex(options, details);
            }
        };
        List<Option> options = List.of(mock(Option.class), mock(Option.class));
        AlgorithmDetails details = AlgorithmDetails.of(null);

        Exceptions.AlgorithmFailureException ex = assertThrows(
                Exceptions.AlgorithmFailureException.class,
                () -> algo.chooseWinnerIndex(options, details)
        );
        assertTrue(ex.getMessage().contains("Interrupted"));
    }

    @Test
    void chooseWinnerIndex_ExecutionException_ThrowsAlgorithmFailureException() {
        ThreadRaceAlgorithm algo = new ThreadRaceAlgorithm() {
            @Override
            public int chooseWinnerIndex(List<Option> options, AlgorithmDetails details) {
                throw new CompletionException(new RuntimeException("Simulated error"));
            }
        };
        List<Option> options = List.of(mock(Option.class), mock(Option.class));
        AlgorithmDetails details = AlgorithmDetails.of(null);

        assertThrows(CompletionException.class, () -> algo.chooseWinnerIndex(options, details));
    }
}