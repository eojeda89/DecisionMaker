package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.command.DecideCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.application.port.out.DecisionRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.*;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DecisionServiceTest {

    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private DecisionAlgorithm algorithm;

    @Captor
    private ArgumentCaptor<Decision> decisionCaptor;

    @Test
    void decide_HappyPath() {
        Map<AlgorithmType, DecisionAlgorithm> algorithms = Map.of(AlgorithmType.THREAD_RACE, algorithm);
        DecisionService service = new DecisionService(decisionRepository, algorithms);

        DecideCommand command = DecideCommand.builder()
                .userId(UserId.of(1L))
                .optionValues(List.of("A", "B", "C"))
                .algorithmType(AlgorithmType.THREAD_RACE)
                .build();
        AlgorithmDetails algorithmDetails = AlgorithmDetails.of(Map.of("seed", 42, "winnerIndex", 1));

        // El algoritmo elige el índice 1 (opción "B")
        when(algorithm.chooseWinnerIndex(anyList())).thenReturn(algorithmDetails);

        // Simular persistencia: primer save asigna ids a opciones, segundo save asigna ganador
        List<Option> optionsWithIds = List.of(
                new Option(new OptionId(10L), "A"),
                new Option(new OptionId(11L), "B"),
                new Option(new OptionId(12L), "C")
        );
        Decision persisted = new Decision(
                new DecisionId(100L),
                new User(UserId.of(1L), null, null, null, null, null),
                AlgorithmType.THREAD_RACE,
                algorithmDetails,
                optionsWithIds,
                null,
                Instant.parse("2024-01-01T00:00:00Z"),
                null,
                "ABCDEFGH"
        );
        Decision finalized = new Decision(
                new DecisionId(100L),
                new User(UserId.of(1L), null, null, null, null, null),
                AlgorithmType.THREAD_RACE,
                algorithmDetails,
                optionsWithIds,
                new OptionId(11L),
                Instant.parse("2024-01-01T00:00:00Z"),
                null,
                "ABCDEFGH"
        );
        when(decisionRepository.save(any())).thenReturn(persisted).thenReturn(finalized);

        DecisionResult result = service.decide(command);

        assertNotNull(result);
        assertEquals(new DecisionId(100L), result.getDecisionId());
        assertEquals(new OptionId(11L), result.getWinningOptionId());
        assertEquals(3, result.getOptions().size());
        assertEquals(AlgorithmType.THREAD_RACE, result.getAlgorithmType());
        assertEquals(42, result.getAlgorithmDetails().getProperties().get("seed"));
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), result.getCreatedAt());
        verify(algorithm).chooseWinnerIndex(anyList());
        verify(decisionRepository, times(2)).save(any());
    }

    @Test
    void decide_NullCommand_ThrowsException() {
        DecisionService service = new DecisionService(decisionRepository, Map.of());
        assertThrows(NullPointerException.class, () -> service.decide(null));
    }

    @Test
    void decide_OptionsNullOrLessThan2_ThrowsException() {
        DecisionService service = new DecisionService(decisionRepository, Map.of());
        DecideCommand cmd1 = DecideCommand.builder()
                .userId(UserId.of(1L))
                .optionValues(null)
                .algorithmType(AlgorithmType.THREAD_RACE)
                .build();
        DecideCommand cmd2 = DecideCommand.builder()
                .userId(UserId.of(1L))
                .optionValues(List.of("A"))
                .algorithmType(AlgorithmType.THREAD_RACE)
                .build();
        assertThrows(Exceptions.InvalidRequestException.class, () -> service.decide(cmd1));
        assertThrows(Exceptions.InvalidRequestException.class, () -> service.decide(cmd2));
    }

    @Test
    void decide_NullAlgorithmType_ThrowsException() {
        DecisionService service = new DecisionService(decisionRepository, Map.of());
        DecideCommand cmd = DecideCommand.builder()
                .userId(UserId.of(1L))
                .optionValues(List.of("A", "B"))
                .algorithmType(null)
                .build();
        assertThrows(Exceptions.InvalidRequestException.class, () -> service.decide(cmd));
    }

    @Test
    void decide_UnsupportedAlgorithm_ThrowsException() {
        DecisionService service = new DecisionService(decisionRepository, Map.of());
        DecideCommand cmd = DecideCommand.builder()
                .userId(UserId.of(1L))
                .optionValues(List.of("A", "B"))
                .algorithmType(AlgorithmType.THREAD_RACE)
                .build();
        assertThrows(Exceptions.UnsupportedAlgorithmException.class, () -> service.decide(cmd));
    }

    @Test
    void decide_AlgorithmReturnsOutOfRangeIndex_ThrowsException() {
        Map<AlgorithmType, DecisionAlgorithm> algorithms = Map.of(AlgorithmType.THREAD_RACE, algorithm);
        DecisionService service = new DecisionService(decisionRepository, algorithms);

        DecideCommand cmd = DecideCommand.builder()
                .userId(UserId.of(1L))
                .optionValues(List.of("A", "B"))
                .algorithmType(AlgorithmType.THREAD_RACE)
                .build();

        AlgorithmDetails algorithmDetails = AlgorithmDetails.of(Map.of("seed", 42, "winnerIndex", 5));

        when(algorithm.chooseWinnerIndex(anyList())).thenReturn(algorithmDetails); // fuera de rango

        assertThrows(Exceptions.DomainValidationException.class, () -> service.decide(cmd));
    }

    @Test
    void listByUser_HappyPath() {
        Map<AlgorithmType, DecisionAlgorithm> algorithms = Map.of();
        DecisionService service = new DecisionService(decisionRepository, algorithms);

        UserId userId = UserId.of(1L);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Decision> decisions = new PageImpl<>(List.of(mock(Decision.class), mock(Decision.class)), pageable, 2);
        when(decisionRepository.findByUser(userId, pageable)).thenReturn(decisions);

        Page<Decision> result = service.listByUser(userId, pageable);

        assertEquals(2, result.getContent().size());
        verify(decisionRepository).findByUser(userId, pageable);
    }

    @Test
    void listByUser_NullUserId_ThrowsException() {
        Map<AlgorithmType, DecisionAlgorithm> algorithms = Map.of();
        DecisionService service = new DecisionService(decisionRepository, algorithms);

        assertThrows(NullPointerException.class, () -> service.listByUser(null, PageRequest.of(0, 20)));
    }

    @Test
    void listByUser_NullPageable_ThrowsException() {
        Map<AlgorithmType, DecisionAlgorithm> algorithms = Map.of();
        DecisionService service = new DecisionService(decisionRepository, algorithms);

        assertThrows(NullPointerException.class, () -> service.listByUser(UserId.of(1L), null));
    }

    @Test
    void getByShareCode_HappyPath() {
        Map<AlgorithmType, DecisionAlgorithm> algorithms = Map.of();
        DecisionService service = new DecisionService(decisionRepository, algorithms);
        Decision decision = mock(Decision.class);
        when(decisionRepository.findByShareCode("ABCDEFGH")).thenReturn(Optional.of(decision));

        Decision result = service.getByShareCode("ABCDEFGH");

        assertEquals(decision, result);
    }

    @Test
    void getByShareCode_UnknownCode_ThrowsResourceNotFound() {
        Map<AlgorithmType, DecisionAlgorithm> algorithms = Map.of();
        DecisionService service = new DecisionService(decisionRepository, algorithms);
        when(decisionRepository.findByShareCode("NOEXISTE")).thenReturn(Optional.empty());

        assertThrows(Exceptions.ResourceNotFoundException.class, () -> service.getByShareCode("NOEXISTE"));
    }

    @Test
    void getByShareCode_NullCode_ThrowsException() {
        Map<AlgorithmType, DecisionAlgorithm> algorithms = Map.of();
        DecisionService service = new DecisionService(decisionRepository, algorithms);

        assertThrows(NullPointerException.class, () -> service.getByShareCode(null));
    }
}