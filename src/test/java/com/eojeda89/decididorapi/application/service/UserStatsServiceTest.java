package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.result.UserStatsResult;
import com.eojeda89.decididorapi.application.port.out.DecisionRepository;
import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.model.OptionId;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStatsServiceTest {

    @Mock
    private DecisionRepository decisionRepository;

    @InjectMocks
    private UserStatsService service;

    private static Decision decisionWith(AlgorithmType algorithmType, String winningValue) {
        OptionId winnerId = OptionId.of(1L);
        Option option = new Option(winnerId, winningValue);
        return Decision.builder()
                .algorithmType(algorithmType)
                .options(List.of(option))
                .winningOptionId(winnerId)
                .build();
    }

    @Test
    void getStats_NullUserId_ThrowsException() {
        assertThrows(NullPointerException.class, () -> service.getStats(null));
    }

    @Test
    void getStats_NoDecisions_ReturnsZeroedResult() {
        UserId userId = UserId.of(1L);
        when(decisionRepository.findAllByUser(userId)).thenReturn(List.of());

        UserStatsResult result = service.getStats(userId);

        assertEquals(0, result.getTotalDecisions());
        assertTrue(result.getDecisionsByAlgorithm().isEmpty());
        assertNull(result.getMostUsedAlgorithm());
        assertTrue(result.getTopWinningOptions().isEmpty());
        assertNull(result.getMostWonOptionValue());
        assertEquals(0, result.getMostWonOptionCount());
    }

    @Test
    void getStats_ComputesAlgorithmBreakdownAndMostUsedAlgorithm() {
        UserId userId = UserId.of(1L);
        List<Decision> decisions = List.of(
                decisionWith(AlgorithmType.DICE_ROLL, "A"),
                decisionWith(AlgorithmType.DICE_ROLL, "B"),
                decisionWith(AlgorithmType.DICE_ROLL, "A"),
                decisionWith(AlgorithmType.FORTUNE_WHEEL, "C")
        );
        when(decisionRepository.findAllByUser(userId)).thenReturn(decisions);

        UserStatsResult result = service.getStats(userId);

        assertEquals(4, result.getTotalDecisions());
        assertEquals(Map.of(AlgorithmType.DICE_ROLL, 3L, AlgorithmType.FORTUNE_WHEEL, 1L),
                result.getDecisionsByAlgorithm());
        assertEquals(AlgorithmType.DICE_ROLL, result.getMostUsedAlgorithm());
    }

    @Test
    void getStats_ComputesTopWinningOptionsAndMostWonOption() {
        UserId userId = UserId.of(1L);
        List<Decision> decisions = List.of(
                decisionWith(AlgorithmType.DICE_ROLL, "Pizza"),
                decisionWith(AlgorithmType.DICE_ROLL, "Pizza"),
                decisionWith(AlgorithmType.DICE_ROLL, "Pizza"),
                decisionWith(AlgorithmType.DICE_ROLL, "Sushi"),
                decisionWith(AlgorithmType.DICE_ROLL, "Sushi"),
                decisionWith(AlgorithmType.DICE_ROLL, "Tacos")
        );
        when(decisionRepository.findAllByUser(userId)).thenReturn(decisions);

        UserStatsResult result = service.getStats(userId);

        assertEquals("Pizza", result.getMostWonOptionValue());
        assertEquals(3, result.getMostWonOptionCount());
        // Orden descendente por victorias
        assertEquals(List.of("Pizza", "Sushi", "Tacos"), List.copyOf(result.getTopWinningOptions().keySet()));
        assertEquals(Map.of("Pizza", 3L, "Sushi", 2L, "Tacos", 1L), result.getTopWinningOptions());
    }

    @Test
    void getStats_TopWinningOptions_LimitedToFive() {
        UserId userId = UserId.of(1L);
        List<Decision> decisions = List.of(
                decisionWith(AlgorithmType.DICE_ROLL, "A"),
                decisionWith(AlgorithmType.DICE_ROLL, "B"),
                decisionWith(AlgorithmType.DICE_ROLL, "C"),
                decisionWith(AlgorithmType.DICE_ROLL, "D"),
                decisionWith(AlgorithmType.DICE_ROLL, "E"),
                decisionWith(AlgorithmType.DICE_ROLL, "F")
        );
        when(decisionRepository.findAllByUser(userId)).thenReturn(decisions);

        UserStatsResult result = service.getStats(userId);

        assertEquals(5, result.getTopWinningOptions().size());
    }

    @Test
    void getStats_DecisionsWithoutWinner_AreIgnoredForWinningOptionStats() {
        UserId userId = UserId.of(1L);
        Decision noWinner = Decision.builder()
                .algorithmType(AlgorithmType.DICE_ROLL)
                .options(List.of(new Option(OptionId.of(1L), "A")))
                .build();
        when(decisionRepository.findAllByUser(userId)).thenReturn(List.of(noWinner));

        UserStatsResult result = service.getStats(userId);

        assertEquals(1, result.getTotalDecisions());
        assertTrue(result.getTopWinningOptions().isEmpty());
        assertNull(result.getMostWonOptionValue());
    }
}
