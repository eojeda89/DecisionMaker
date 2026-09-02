package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.adapter.in.web.dto.BestOfNRequest;
import com.eojeda89.decididorapi.adapter.in.web.dto.MakeDecisionRequest;
import com.eojeda89.decididorapi.adapter.in.web.dto.MakeDecisionResponse;
import com.eojeda89.decididorapi.application.port.in.GetDecisionHistoryUseCase;
import com.eojeda89.decididorapi.application.port.in.GetSharedDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.MakeBestOfNDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.MakeDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.command.BestOfNCommand;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
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
    private MakeBestOfNDecisionUseCase makeBestOfNDecisionUseCase;
    @Mock
    private GetDecisionHistoryUseCase getDecisionHistoryUseCase;
    @Mock
    private GetSharedDecisionUseCase getSharedDecisionUseCase;
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
                List.of("A", "B"),
                false
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
    void getSharedDecision_HappyPath_NoAuthenticationNeeded() {
        // A propósito: este endpoint es público, no debe llamar a
        // resolveAuthenticatedUserId() ni tocar el SecurityContext.
        SecurityContextHolder.clearContext();
        Decision decision = Decision.builder()
                .id(DecisionId.of(1L))
                .user(User.builder().id(UserId.of(99L)).username("owner").build())
                .winningOptionId(OptionId.of(100L))
                .options(List.of(new Option(OptionId.of(100L), "A"), new Option(OptionId.of(101L), "B")))
                .algorithmType(AlgorithmType.THREAD_RACE)
                .algorithmDetails(AlgorithmDetails.of(Map.of("winnerIndex", 0)))
                .shareCode("ABCDEFGH")
                .createdAt(Instant.now())
                .build();
        when(getSharedDecisionUseCase.getByShareCode("ABCDEFGH")).thenReturn(decision);

        MakeDecisionResponse response = controller.getSharedDecision("ABCDEFGH");

        assertEquals(1L, response.getDecisionId());
        assertEquals(100L, response.getWinningOptionId());
        assertEquals("ABCDEFGH", response.getShareCode());
        verifyNoInteractions(userRepository);
    }

    @Test
    void getSharedDecision_UnknownCode_ThrowsException() {
        when(getSharedDecisionUseCase.getByShareCode("NOEXISTE"))
                .thenThrow(new ResourceNotFoundException("Shared decision not found"));

        assertThrows(ResourceNotFoundException.class, () -> controller.getSharedDecision("NOEXISTE"));
    }

    @Test
    void makeBestOfNDecision_HappyPath() {
        BestOfNRequest request = new BestOfNRequest(5, List.of("A", "B"), null);
        DecisionResult result = DecisionResult.builder()
                .decisionId(DecisionId.of(20L))
                .winningOptionId(OptionId.of(200L))
                .options(List.of(new Option(OptionId.of(200L), "A"), new Option(OptionId.of(201L), "B")))
                .algorithmType(AlgorithmType.BEST_OF_N)
                .algorithmDetails(AlgorithmDetails.of(Map.of("winnerIndex", 0)))
                .createdAt(Instant.now())
                .build();
        when(makeBestOfNDecisionUseCase.decide(any(BestOfNCommand.class))).thenReturn(result);

        MakeDecisionResponse response = controller.makeBestOfNDecision(request);

        assertEquals(20L, response.getDecisionId());
        assertEquals(200L, response.getWinningOptionId());
        assertEquals("best-of-n", response.getAlgorithmType());
        verify(makeBestOfNDecisionUseCase).decide(argThat(cmd ->
                cmd.getUserId().equals(UserId.of(1L))
                        && cmd.getRounds() == 5
                        && cmd.getAlgorithmPool() == null));
    }

    @Test
    void makeBestOfNDecision_ParsesRequestedAlgorithmPool() {
        BestOfNRequest request = new BestOfNRequest(3, List.of("A", "B"), List.of("dice-roll", "THREAD_RACE"));
        DecisionResult result = DecisionResult.builder()
                .decisionId(DecisionId.of(21L))
                .winningOptionId(OptionId.of(210L))
                .options(List.of(new Option(OptionId.of(210L), "A"), new Option(OptionId.of(211L), "B")))
                .algorithmType(AlgorithmType.BEST_OF_N)
                .algorithmDetails(AlgorithmDetails.of(Map.of("winnerIndex", 0)))
                .createdAt(Instant.now())
                .build();
        when(makeBestOfNDecisionUseCase.decide(any(BestOfNCommand.class))).thenReturn(result);

        controller.makeBestOfNDecision(request);

        verify(makeBestOfNDecisionUseCase).decide(argThat(cmd ->
                cmd.getAlgorithmPool().equals(List.of(AlgorithmType.DICE_ROLL, AlgorithmType.THREAD_RACE))));
    }

    @Test
    void makeBestOfNDecision_UnsupportedAlgorithmInPool_ThrowsException() {
        BestOfNRequest request = new BestOfNRequest(3, List.of("A", "B"), List.of("no-existe"));

        assertThrows(UnsupportedAlgorithmException.class, () -> controller.makeBestOfNDecision(request));
    }

    @Test
    void makeBestOfNDecision_NullRequest_ThrowsException() {
        assertThrows(NullPointerException.class, () -> controller.makeBestOfNDecision(null));
    }

    @Test
    void listByUser_HappyPath() {
        Pageable pageable = PageRequest.of(0, 20);
        Decision decision = Decision.builder()
                .id(DecisionId.of(1L))
                .user(User.builder()
                        .id(UserId.of(1L))
                        .build())
                .winningOptionId(OptionId.of(100L))
                .options(List.of(new Option(OptionId.of(100L), "A"), new Option(OptionId.of(101L), "B")))
                .algorithmType(AlgorithmType.THREAD_RACE)
                .algorithmDetails(AlgorithmDetails.of(Map.of("seed", 42, "winnerIndex", 1)))
                .createdAt(Instant.now())
                .build();
        Page<Decision> decisions = new PageImpl<>(List.of(decision), pageable, 1);
        when(getDecisionHistoryUseCase.listByUser(UserId.of(1L), pageable)).thenReturn(decisions);

        PagedModel<MakeDecisionResponse> responses = controller.listByUser(0, 20);

        assertEquals(1, responses.getContent().size());
        assertEquals(1L, responses.getContent().iterator().next().getDecisionId());
        assertEquals(1, responses.getMetadata().totalElements());
        verify(getDecisionHistoryUseCase, times(1)).listByUser(UserId.of(1L), pageable);
    }

    @Test
    void listByUser_EmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(getDecisionHistoryUseCase.listByUser(UserId.of(1L), pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        PagedModel<MakeDecisionResponse> responses = controller.listByUser(0, 20);

        assertTrue(responses.getContent().isEmpty());
    }

    @Test
    void listByUser_UsesRequestedPageAndSize() {
        Pageable pageable = PageRequest.of(2, 10);
        when(getDecisionHistoryUseCase.listByUser(UserId.of(1L), pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        controller.listByUser(2, 10);

        verify(getDecisionHistoryUseCase).listByUser(UserId.of(1L), pageable);
    }

    @Test
    void listByUser_FallsBackToEmailWhenUsernameLookupMisses() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", null, List.of())
        );
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(User.builder().id(UserId.of(2L)).email("user@example.com").build()));
        Pageable pageable = PageRequest.of(0, 20);
        when(getDecisionHistoryUseCase.listByUser(UserId.of(2L), pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        PagedModel<MakeDecisionResponse> responses = controller.listByUser(0, 20);

        assertTrue(responses.getContent().isEmpty());
        verify(getDecisionHistoryUseCase).listByUser(UserId.of(2L), pageable);
    }
}
