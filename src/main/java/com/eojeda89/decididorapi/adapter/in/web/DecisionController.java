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
import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import com.eojeda89.decididorapi.domain.model.User;
import com.eojeda89.decididorapi.domain.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Decisiones", description = "Toma de decisiones al azar y consulta de historial")
public class DecisionController {

    private static final int MAX_PAGE_SIZE = 100;

    private final MakeDecisionUseCase makeDecisionUseCase;
    private final MakeBestOfNDecisionUseCase makeBestOfNDecisionUseCase;
    private final GetDecisionHistoryUseCase getDecisionHistoryUseCase;
    private final GetSharedDecisionUseCase getSharedDecisionUseCase;
    private final AlgorithmDetailsLocalizer algorithmDetailsLocalizer;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Toma una nueva decisión", description = "Elige un ganador entre las opciones dadas usando el algoritmo indicado y persiste el resultado, para el usuario autenticado.")
    public MakeDecisionResponse makeDecision(@Valid @RequestBody MakeDecisionRequest request) {
        Objects.requireNonNull(request, "request");
        AlgorithmType type = parseAlgorithmType(request.getAlgorithmType());
        DecideCommand command = new DecideCommand(
                resolveAuthenticatedUserId(),
                type,
                request.getOptions()
        );
        DecisionResult result = makeDecisionUseCase.decide(command);
        MakeDecisionResponse response = MakeDecisionResponse.fromResult(result);
        response.setAlgorithmDetails(algorithmDetailsLocalizer.localize(result.getAlgorithmDetails()));
        return response;
    }

    @PostMapping("/best-of-n")
    @Operation(summary = "Toma una decisión al mejor de N rondas", description = "Corre N (3, 5 o 7) rondas independientes, cada una con un algoritmo elegido al azar (o del subconjunto indicado en \"algorithms\"), y persiste el resultado para el usuario autenticado. Gana la opción con más rondas ganadas; empate en el primer puesto se resuelve con una ronda extra solo entre las empatadas.")
    public MakeDecisionResponse makeBestOfNDecision(@Valid @RequestBody BestOfNRequest request) {
        Objects.requireNonNull(request, "request");
        List<AlgorithmType> pool = request.getAlgorithms() == null
                ? null
                : request.getAlgorithms().stream().map(this::parseAlgorithmType).toList();
        BestOfNCommand command = BestOfNCommand.builder()
                .userId(resolveAuthenticatedUserId())
                .rounds(request.getRounds())
                .optionValues(request.getOptions())
                .algorithmPool(pool)
                .build();
        DecisionResult result = makeBestOfNDecisionUseCase.decide(command);
        MakeDecisionResponse response = MakeDecisionResponse.fromResult(result);
        response.setAlgorithmDetails(algorithmDetailsLocalizer.localize(result.getAlgorithmDetails()));
        return response;
    }

    @GetMapping("/shared/{shareCode}")
    @Operation(summary = "Consulta una decisión compartida por su código", description = "Público, sin autenticación (Fase 3.3 \"salas compartidas\"): cualquiera con el código ve el mismo resultado y su explicación, para compartir sin convertirlo en una encuesta.")
    public MakeDecisionResponse getSharedDecision(@PathVariable String shareCode) {
        var decision = getSharedDecisionUseCase.getByShareCode(shareCode);
        MakeDecisionResponse response = MakeDecisionResponse.fromDomain(decision);
        response.setAlgorithmDetails(algorithmDetailsLocalizer.localize(decision.getAlgorithmDetails()));
        return response;
    }

    @GetMapping
    @Operation(summary = "Lista el historial de decisiones del usuario autenticado, paginado", description = "Ordenado por más reciente primero.")
    public PagedModel<MakeDecisionResponse> listByUser(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        var responsePage = getDecisionHistoryUseCase.listByUser(resolveAuthenticatedUserId(), pageable)
                .map(decision -> {
                    MakeDecisionResponse response = MakeDecisionResponse.fromDomain(decision);
                    response.setAlgorithmDetails(algorithmDetailsLocalizer.localize(decision.getAlgorithmDetails()));
                    return response;
                });
        // PagedModel en vez de devolver Page directo: Page/PageImpl no tiene
        // un contrato JSON estable para una API pública (Spring emite un
        // warning al respecto). PagedModel sí lo tiene: {content, page: {...}}.
        return new PagedModel<>(responsePage);
    }

    // Quién decide/consulta se determina siempre del JWT autenticado, nunca
    // de un userId provisto por el cliente — evita que un usuario opere
    // sobre datos de otro con solo cambiar ese valor en el request.
    private UserId resolveAuthenticatedUserId() {
        String principalName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(principalName)
                .or(() -> userRepository.findByEmail(principalName))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        return user.getId();
    }

    private AlgorithmType parseAlgorithmType(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestException("algorithmType is required");
        }
        String norm = value.trim();
        // try name first (case-insensitive)
        for (AlgorithmType t : AlgorithmType.values()) {
            if (t.name().equalsIgnoreCase(norm)) return t;
        }
        // then try code (case-insensitive)
        String lower = norm.toLowerCase(Locale.ROOT);
        for (AlgorithmType t : AlgorithmType.values()) {
            if (t.getCode().equalsIgnoreCase(lower)) return t;
        }
        throw new UnsupportedAlgorithmException("Unsupported algorithm: " + value);
    }
}
