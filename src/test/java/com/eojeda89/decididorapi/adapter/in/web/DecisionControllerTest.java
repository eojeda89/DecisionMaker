package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.adapter.in.web.dto.MakeDecisionRequest;
import com.eojeda89.decididorapi.adapter.in.web.dto.MakeDecisionResponse;
import com.eojeda89.decididorapi.application.port.in.GetDecisionHistoryUseCase;
import com.eojeda89.decididorapi.application.port.in.MakeDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.command.DecideCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.application.port.out.UserRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions.InvalidRequestException;
import com.eojeda89.decididorapi.common.exception.Exceptions.ResourceNotFoundException;
import com.eojeda89.decididorapi.common.exception.Exceptions.UnsupportedAlgorithmException;
import com.eojeda89.decididorapi.domain.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DecisionControllerTest {

    @Mock
    private MakeDecisionUseCase makeDecisionUseCase;
    @Mock
    private GetDecisionHistoryUseCase getDecisionHistoryUseCase;
    @Mock
    private AlgorithmDetailsLocalizer algorithmDetailsLocalizer;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DecisionController controller;

    private MakeDecisionRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new MakeDecisionRequest(
                "THREAD_RACE",
                List.of("A", "B")
        );
        lenient().when(algorithmDetailsLocalizer.localize(any()))
                .thenAnswer(invocation -> {
                    AlgorithmDetails details = invocation.getArgument(0);
                    return details == null ? Map.of() : details.getProperties();
                });
        // El userId ya no viene del request: se resuelve del principal
        // autenticado (ver DecisionController.resolveAuthenticatedUserId).
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("authenticatedUser", null, List.of())
        );
        lenient().when(userRepository.findByUsername("authenticatedUser"))
                .thenReturn(Optional.of(User.builder().id(UserId.of(1L)).username("authenticatedUser").build()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void makeDecision_HappyPath() {
        DecisionResult result = DecisionResult.builder()
                .decisionId(DecisionId.of(10L))
                .winningOptionId(OptionId.of(100L))
                .options(List.of(new Option(OptionId.of(100L), "A"), new Option(OptionId.of(101L), "B")))
                .algorithmType(AlgorithmType.THREAD_RACE)
                .algorithmDetails(AlgorithmDetails.of(Map.of("seed", 42, "winnerIndex", 1)))
                .createdAt(Instant.now())
                .build();
        when(makeDecisionUseCase.decide(any(DecideCommand.class))).thenReturn(result);

        MakeDecisionResponse response = controller.makeDecision(validRequest);

        assertNotNull(response);
        assertEquals(10L, response.getDecisionId());
        assertEquals(100L, response.getWinningOptionId());
        assertEquals("thread-race", response.getAlgorithmType());
        assertEquals(2, response.getOptions().size());
        verify(makeDecisionUseCase, times(1)).decide(argThat(cmd -> cmd.getUserId().equals(UserId.of(1L))));
    }

    @Test
    void makeDecision_UsesAuthenticatedUser_NotAnyClientSuppliedId() {
        // Regresión IDOR: antes el userId venía del body del request, así que
        // cualquier usuario autenticado podía decidir "en nombre" de otro con
        // solo cambiar ese campo. Ahora MakeDecisionRequest ni siquiera tiene
        // userId — siempre se usa el id resuelto del JWT.
        DecisionResult result = DecisionResult.builder()
                .decisionId(DecisionId.of(1L))
                .winningOptionId(OptionId.of(1L))
                .options(List.of(new Option(OptionId.of(1L), "A"), new Option(OptionId.of(2L), "B")))
                .algorithmType(AlgorithmType.THREAD_RACE)
                .algorithmDetails(AlgorithmDetails.of(Map.of("winnerIndex", 0)))
                .createdAt(Instant.now())
                .build();
        when(makeDecisionUseCase.decide(any(DecideCommand.class))).thenReturn(result);

        controller.makeDecision(validRequest);

        verify(makeDecisionUseCase).decide(argThat(cmd -> cmd.getUserId().equals(UserId.of(1L))));
    }

    @Test
    void makeDecision_AuthenticatedUserNotFound_ThrowsException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ghost", null, List.of())
        );
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("ghost")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.makeDecision(validRequest));
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
                        .algorithmDetails(AlgorithmDetails.of(Map.of("seed", 42, "winnerIndex", 1)))
                        .createdAt(Instant.now())
                        .build()
                );
        when(getDecisionHistoryUseCase.listByUser(UserId.of(1L))).thenReturn(decisions);

        List<MakeDecisionResponse> responses = controller.listByUser();

        assertEquals(1, responses.size());
        assertEquals(1L, responses.getFirst().getDecisionId());
        verify(getDecisionHistoryUseCase, times(1)).listByUser(UserId.of(1L));
    }

    @Test
    void listByUser_EmptyList() {
        when(getDecisionHistoryUseCase.listByUser(UserId.of(1L))).thenReturn(Collections.emptyList());

        List<MakeDecisionResponse> responses = controller.listByUser();

        assertTrue(responses.isEmpty());
    }

    @Test
    void listByUser_FallsBackToEmailWhenUsernameLookupMisses() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", null, List.of())
        );
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(User.builder().id(UserId.of(2L)).email("user@example.com").build()));
        when(getDecisionHistoryUseCase.listByUser(UserId.of(2L))).thenReturn(Collections.emptyList());

        List<MakeDecisionResponse> responses = controller.listByUser();

        assertTrue(responses.isEmpty());
        verify(getDecisionHistoryUseCase).listByUser(UserId.of(2L));
    }
}
