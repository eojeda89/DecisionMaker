package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RandomizedVotingTest {

    private final RandomizedVoting algorithm = new RandomizedVoting();

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
    void chooseWinnerIndex_OptionsEmpty_ThrowsException() {
        List<Option> options = List.of();
        assertThrows(Exceptions.InvalidRequestException.class, () -> algorithm.chooseWinnerIndex(options));
    }

    @Test
    void chooseWinnerIndex_OptionsWithOneElement_ThrowsException() {
        List<Option> options = List.of(mock(Option.class));
        assertThrows(Exceptions.InvalidRequestException.class, () -> algorithm.chooseWinnerIndex(options));
    }

    @Test
    void chooseWinnerIndex_OverManyTrials_DistributionIsApproximatelyUniform() {
        // Cada opción recibe un voto aleatorio independiente (0-99); gana el
        // voto más alto. Al ser i.i.d., cada opción debería ganar ~1/n de
        // las veces (empates son posibles pero no sesgan la distribución de
        // forma apreciable con un margen de ±5 puntos porcentuales).
        List<Option> options = List.of(mock(Option.class), mock(Option.class), mock(Option.class), mock(Option.class));
        int trials = 10_000;
        int[] wins = new int[options.size()];

        for (int i = 0; i < trials; i++) {
            int index = algorithm.chooseWinnerIndex(options).get("winnerIndex", Integer.class);
            wins[index]++;
        }

        for (int index = 0; index < wins.length; index++) {
            double rate = wins[index] / (double) trials;
            assertTrue(rate > 0.20 && rate < 0.30,
                    "Índice " + index + " ganó " + rate + " de las veces, esperado ~0.25 (±0.05)");
        }
    }
}
