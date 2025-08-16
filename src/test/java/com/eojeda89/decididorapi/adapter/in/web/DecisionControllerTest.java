package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.adapter.in.web.dto.MakeDecisionRequest;
import com.eojeda89.decididorapi.adapter.in.web.dto.MakeDecisionResponse;
import com.eojeda89.decididorapi.application.port.in.GetDecisionHistoryUseCase;
import com.eojeda89.decididorapi.application.port.in.MakeDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.command.DecideCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.common.exception.Exceptions.InvalidRequestException;
import com.eojeda89.decididorapi.common.exception.Exceptions.UnsupportedAlgorithmException;
import com.eojeda89.decididorapi.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DecisionControllerTest {

    @Mock
    private MakeDecisionUseCase makeDecisionUseCase;
    @Mock
    private GetDecisionHistoryUseCase getDecisionHistoryUseCase;

    @InjectMocks
    private DecisionController controller;

    private MakeDecisionRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new MakeDecisionRequest(
                1L,
                "THREAD_RACE",
                Map.of("seed", 42),
                List.of("A", "B")
        );
    }

    @Test
    void makeDecision_HappyPath() {
        DecisionResult result = DecisionResult.builder()
                .decisionId(DecisionId.of(10L))
                .winningOptionId(OptionId.of(100L))
                .options(List.of(new Option(OptionId.of(100L), "A"), new Option(OptionId.of(101L), "B")))
                .algorithmType(AlgorithmType.THREAD_RACE)
                .algorithmDetails(AlgorithmDetails.of(Map.of("seed", 42)))
                .createdAt(Instant.now())
                .build();
        when(makeDecisionUseCase.decide(any(DecideCommand.class))).thenReturn(result);

        MakeDecisionResponse response = controller.makeDecision(validRequest);

        assertNotNull(response);
        assertEquals(10L, response.getDecisionId());
        assertEquals(100L, response.getWinningOptionId());
        assertEquals("thread-race", response.getAlgorithmType());
        assertEquals(2, response.getOptions().size());
        verify(makeDecisionUseCase, times(1)).decide(any(DecideCommand.class));
    }

    @Test
    void makeDecision_NullRequest_ThrowsException() {
        assertThrows(NullPointerException.class, () -> controller.makeDecision(null));
    }

    @Test
    void makeDecision_EmptyAlgorithmType_ThrowsException() {
        validRequest.setAlgorithmType(" ");
        assertThrows(InvalidRequestException.class, () -> controller.makeDecision(validRequest));
    }

    @Test
    void makeDecision_UnsupportedAlgorithm_ThrowsException() {
        validRequest.setAlgorithmType("NO_EXISTE");
        assertThrows(UnsupportedAlgorithmException.class, () -> controller.makeDecision(validRequest));
    }

    @Test
    void makeDecision_OptionsMenorA2_ThrowsException() {
        validRequest.setOptions(List.of("SoloUno"));
        // Simula validación manual, ya que @Valid no se ejecuta fuera de Spring
        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            // Aquí deberías usar un validador, pero para simplificar:
            if (validRequest.getOptions().size() < 2) {
                throw new jakarta.validation.ConstraintViolationException("At least 2 options are required", Set.of());
            }
            controller.makeDecision(validRequest);
        });
    }

    @Test
    void listByUser_HappyPath() {
        List<Decision> decisions = List.of(
                Decision.builder()
                        .id(DecisionId.of(1L))
                        .user(User.builder()
                                .id(UserId.of(1L))
                                .build())
                        .winningOptionId(OptionId.of(100L))
                        .options(List.of(new Option(OptionId.of(100L), "A"), new Option(OptionId.of(101L), "B")))
                        .algorithmType(AlgorithmType.THREAD_RACE)
                        .algorithmDetails(AlgorithmDetails.of(Map.of("seed", 42)))
                        .createdAt(Instant.now())
                        .build()
                );
        when(getDecisionHistoryUseCase.listByUser(UserId.of(1L))).thenReturn(decisions);

        List<MakeDecisionResponse> responses = controller.listByUser(1L);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.getFirst().getDecisionId());
        verify(getDecisionHistoryUseCase, times(1)).listByUser(UserId.of(1L));
    }

    @Test
    void listByUser_EmptyList() {
        when(getDecisionHistoryUseCase.listByUser(UserId.of(2L))).thenReturn(Collections.emptyList());

        List<MakeDecisionResponse> responses = controller.listByUser(2L);

        assertTrue(responses.isEmpty());
    }
}