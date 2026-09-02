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
class FortuneWheelAlgorithmTest {

    private final FortuneWheelAlgorithm algorithm = new FortuneWheelAlgorithm();

    @Test
    void chooseWinnerIndex_HappyPath_ReturnsValidIndex() {
        List<Option> options = List.of(mock(Option.class), mock(Option.class), mock(Option.class), mock(Option.class));

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
    void chooseWinnerIndex_AlwaysReturnsIndexWithinBounds() {
        List<Option> options = List.of(mock(Option.class), mock(Option.class), mock(Option.class));

        // Ejecutar varias veces para cubrir aleatoriedad
        for (int i = 0; i < 100; i++) {
            AlgorithmDetails algorithmDetails = algorithm.chooseWinnerIndex(options);
            int index = algorithmDetails.get("winnerIndex", Integer.class);
            assertTrue(index >= 0 && index < options.size(), "Índice fuera de rango: " + index);
        }
    }

    @Test
    void chooseWinnerIndex_ReturnsWinningAngleDegreesConsistentWithWinnerIndex() {
        // Fase 4.1: la vista usa este campo (sin prefijo "custom_", no
        // formateado) para animar la ruleta girando hasta el ángulo real.
        List<Option> options = List.of(mock(Option.class), mock(Option.class), mock(Option.class), mock(Option.class));

        for (int i = 0; i < 50; i++) {
            AlgorithmDetails details = algorithm.chooseWinnerIndex(options);
            double angle = details.get("winningAngleDegrees", Double.class);
            int index = details.get("winnerIndex", Integer.class);

            assertTrue(angle >= 0.0 && angle < 360.0, "Ángulo fuera de rango: " + angle);
            double segment = 360.0 / options.size();
            assertEquals(index, Math.min((int) Math.floor(angle / segment), options.size() - 1),
                    "El índice ganador debe corresponder al segmento del ángulo devuelto");
        }
    }

    @Test
    void chooseWinnerIndex_OverManyTrials_DistributionIsApproximatelyUniform() {
        // Los segmentos de la ruleta son del mismo tamaño (360/n grados), así
        // que cada opción debería ganar ~1/n de las veces.
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