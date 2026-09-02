package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.command.BestOfNCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.application.port.out.DecisionRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.*;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class BestOfNDecisionServiceTest {

    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private DecisionAlgorithm algorithm;

    @Captor
    private ArgumentCaptor<Decision> decisionCaptor;

    @Test
    void decide_NullCommand_ThrowsException() {
        BestOfNDecisionService service = new BestOfNDecisionService(decisionRepository, Map.of());
        assertThrows(NullPointerException.class, () -> service.decide(null));
    }

    @Test
    void decide_FewerThanTwoOptions_ThrowsException() {
        BestOfNDecisionService service = new BestOfNDecisionService(decisionRepository, Map.of());
        BestOfNCommand command = BestOfNCommand.builder()
                .userId(UserId.of(1L)).rounds(3).optionValues(List.of("SoloUno")).build();

        assertThrows(Exceptions.InvalidRequestException.class, () -> service.decide(command));
    }

    @Test
    void decide_EvenRounds_ThrowsException() {
        BestOfNDecisionService service = new BestOfNDecisionService(decisionRepository, Map.of());
        BestOfNCommand command = BestOfNCommand.builder()
                .userId(UserId.of(1L)).rounds(4).optionValues(List.of("A", "B")).build();

        assertThrows(Exceptions.InvalidRequestException.class, () -> service.decide(command));
    }

    @Test
    void decide_UnsupportedAlgorithmInPool_ThrowsException() {
        BestOfNDecisionService service = new BestOfNDecisionService(decisionRepository, Map.of());
        BestOfNCommand command = BestOfNCommand.builder()
                .userId(UserId.of(1L)).rounds(3).optionValues(List.of("A", "B"))
                .algorithmPool(List.of(AlgorithmType.DICE_ROLL))
                .build();

        assertThrows(Exceptions.UnsupportedAlgorithmException.class, () -> service.decide(command));
    }

    @Test
    @SuppressWarnings("unchecked")
    void decide_ClearMajority_WinnerIsMostFrequentRoundWinner() {
        // Pool de un solo algoritmo: fuerza que sea siempre el que corre, sin
        // depender del sorteo de CUÁL algoritmo se usa por ronda -- solo
        // controlamos, vía stubbing secuencial, qué índice gana cada ronda.
        Map<AlgorithmType, DecisionAlgorithm> algorithms = Map.of(AlgorithmType.DICE_ROLL, algorithm);
        BestOfNDecisionService service = new BestOfNDecisionService(decisionRepository, algorithms);

        // 3 rondas: gana el índice 0, 0, 1 -> índice 0 gana 2 de 3 (mayoría clara)
        when(algorithm.chooseWinnerIndex(anyList()))
                .thenReturn(AlgorithmDetails.of(Map.of("winnerIndex", 0)))
                .thenReturn(AlgorithmDetails.of(Map.of("winnerIndex", 0)))
                .thenReturn(AlgorithmDetails.of(Map.of("winnerIndex", 1)));

        List<Option> optionsWithIds = List.of(new Option(new OptionId(10L), "A"), new Option(new OptionId(11L), "B"));
        Decision afterFirstSave = Decision.builder()
                .id(new DecisionId(100L)).user(User.builder().id(UserId.of(1L)).build())
                .algorithmType(AlgorithmType.BEST_OF_N).options(optionsWithIds).build();
        Decision finalized = Decision.builder()
                .id(new DecisionId(100L)).user(User.builder().id(UserId.of(1L)).build())
                .algorithmType(AlgorithmType.BEST_OF_N).options(optionsWithIds)
                .winningOptionId(new OptionId(10L)).createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
        when(decisionRepository.save(any(Decision.class))).thenReturn(afterFirstSave).thenReturn(finalized);

        BestOfNCommand command = BestOfNCommand.builder()
                .userId(UserId.of(1L)).rounds(3).optionValues(List.of("A", "B")).build();
        DecisionResult result = service.decide(command);

        assertEquals(10L, result.getWinningOptionId().value());
        verify(algorithm, times(3)).chooseWinnerIndex(anyList());
        verify(decisionRepository, times(2)).save(decisionCaptor.capture());

        Decision firstSaveArg = decisionCaptor.getAllValues().get(0);
        assertEquals(AlgorithmType.BEST_OF_N, firstSaveArg.getAlgorithmType());
        assertEquals(0, firstSaveArg.getAlgorithmDetails().get("winnerIndex", Integer.class));
        List<Map<String, Object>> steps = (List<Map<String, Object>>) firstSaveArg.getAlgorithmDetails().get("steps", List.class);
        assertEquals(4, steps.size(), "3 pasos de ronda + 1 de resultado final, sin desempate");
    }

    @Test
    @SuppressWarnings("unchecked")
    void decide_TiedFirstPlace_RunsExtraTieBreakRoundAmongLeadersOnly() {
        Map<AlgorithmType, DecisionAlgorithm> algorithms = Map.of(AlgorithmType.DICE_ROLL, algorithm);
        BestOfNDecisionService service = new BestOfNDecisionService(decisionRepository, algorithms);

        // 3 opciones, 3 rondas: cada una gana una ronda -> triple empate 1-1-1.
        // La 4ª llamada (desempate) corre solo sobre las 3 opciones empatadas
        // y elige localIndex 1 (la 2ª de la lista de empatados).
        when(algorithm.chooseWinnerIndex(anyList()))
                .thenReturn(AlgorithmDetails.of(Map.of("winnerIndex", 0)))
                .thenReturn(AlgorithmDetails.of(Map.of("winnerIndex", 1)))
                .thenReturn(AlgorithmDetails.of(Map.of("winnerIndex", 2)))
                .thenReturn(AlgorithmDetails.of(Map.of("winnerIndex", 1)));

        List<Option> optionsWithIds = List.of(
                new Option(new OptionId(10L), "A"), new Option(new OptionId(11L), "B"), new Option(new OptionId(12L), "C"));
        Decision afterFirstSave = Decision.builder()
                .id(new DecisionId(100L)).user(User.builder().id(UserId.of(1L)).build())
                .algorithmType(AlgorithmType.BEST_OF_N).options(optionsWithIds).build();
        Decision finalized = Decision.builder()
                .id(new DecisionId(100L)).user(User.builder().id(UserId.of(1L)).build())
                .algorithmType(AlgorithmType.BEST_OF_N).options(optionsWithIds)
                .winningOptionId(new OptionId(11L)).createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
        when(decisionRepository.save(any(Decision.class))).thenReturn(afterFirstSave).thenReturn(finalized);

        BestOfNCommand command = BestOfNCommand.builder()
                .userId(UserId.of(1L)).rounds(3).optionValues(List.of("A", "B", "C")).build();
        DecisionResult result = service.decide(command);

        // El índice 1 (B, id 11) gana el desempate entre los 3 empatados.
        assertEquals(11L, result.getWinningOptionId().value());
        verify(algorithm, times(4)).chooseWinnerIndex(anyList());

        verify(decisionRepository, times(2)).save(decisionCaptor.capture());
        Decision firstSaveArg = decisionCaptor.getAllValues().get(0);
        assertEquals(1, firstSaveArg.getAlgorithmDetails().get("winnerIndex", Integer.class));
        List<Map<String, Object>> steps = (List<Map<String, Object>>) firstSaveArg.getAlgorithmDetails().get("steps", List.class);
        assertEquals(5, steps.size(), "3 pasos de ronda + 1 de desempate + 1 de resultado final");
        assertEquals("narrative.best-of-n.tiebreak", steps.get(3).get("descriptionKey"));
    }

    @Test
    void decide_DefaultPool_DrawsFromAllRegisteredAlgorithms() {
        Map<AlgorithmType, DecisionAlgorithm> algorithms = Map.of(
                AlgorithmType.DICE_ROLL, algorithm,
                AlgorithmType.THREAD_RACE, algorithm
        );
        BestOfNDecisionService service = new BestOfNDecisionService(decisionRepository, algorithms);
        when(algorithm.chooseWinnerIndex(anyList())).thenReturn(AlgorithmDetails.of(Map.of("winnerIndex", 0)));

        List<Option> optionsWithIds = List.of(new Option(new OptionId(10L), "A"), new Option(new OptionId(11L), "B"));
        Decision afterFirstSave = Decision.builder().id(new DecisionId(100L))
                .user(User.builder().id(UserId.of(1L)).build())
                .algorithmType(AlgorithmType.BEST_OF_N).options(optionsWithIds).build();
        Decision finalized = Decision.builder().id(new DecisionId(100L))
                .user(User.builder().id(UserId.of(1L)).build())
                .algorithmType(AlgorithmType.BEST_OF_N).options(optionsWithIds)
                .winningOptionId(new OptionId(10L)).build();
        when(decisionRepository.save(any(Decision.class))).thenReturn(afterFirstSave).thenReturn(finalized);

        BestOfNCommand command = BestOfNCommand.builder()
                .userId(UserId.of(1L)).rounds(3).optionValues(List.of("A", "B")).build();

        assertDoesNotThrow(() -> service.decide(command));
        // No lanza UnsupportedAlgorithmException: ambos algoritmos registrados
        // son válidos para el pool por default (sin algorithmPool explícito).
        verify(algorithm, times(3)).chooseWinnerIndex(anyList());
    }
}
