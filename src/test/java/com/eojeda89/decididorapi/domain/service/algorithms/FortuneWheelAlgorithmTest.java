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
        AlgorithmDetails details = AlgorithmDetails.of(null);

        int index = algorithm.chooseWinnerIndex(options, details);

        assertTrue(index >= 0 && index < options.size());
    }

    @Test
    void chooseWinnerIndex_OptionsIsNull_ThrowsException() {
        assertThrows(NullPointerException.class, () -> algorithm.chooseWinnerIndex(null, AlgorithmDetails.of(null)));
    }

    @Test
    void chooseWinnerIndex_OptionsEmpty_ThrowsException() {
        List<Option> options = List.of();
        assertThrows(Exceptions.InvalidRequestException.class, () -> algorithm.chooseWinnerIndex(options, AlgorithmDetails.of(null)));
    }

    @Test
    void chooseWinnerIndex_OptionsWithOneElement_ThrowsException() {
        List<Option> options = List.of(mock(Option.class));
        assertThrows(Exceptions.InvalidRequestException.class, () -> algorithm.chooseWinnerIndex(options, AlgorithmDetails.of(null)));
    }

    @Test
    void chooseWinnerIndex_AlwaysReturnsIndexWithinBounds() {
        List<Option> options = List.of(mock(Option.class), mock(Option.class), mock(Option.class));
        AlgorithmDetails details = AlgorithmDetails.of(null);

        // Ejecutar varias veces para cubrir aleatoriedad
        for (int i = 0; i < 100; i++) {
            int index = algorithm.chooseWinnerIndex(options, details);
            assertTrue(index >= 0 && index < options.size(), "Índice fuera de rango: " + index);
        }
    }
}