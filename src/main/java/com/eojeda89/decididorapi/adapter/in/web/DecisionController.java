package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.adapter.in.web.dto.MakeDecisionRequest;
import com.eojeda89.decididorapi.adapter.in.web.dto.MakeDecisionResponse;
import com.eojeda89.decididorapi.application.port.in.GetDecisionHistoryUseCase;
import com.eojeda89.decididorapi.application.port.in.MakeDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.command.DecideCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.common.exception.Exceptions.InvalidRequestException;
import com.eojeda89.decididorapi.common.exception.Exceptions.UnsupportedAlgorithmException;
import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final MakeDecisionUseCase makeDecisionUseCase;
    private final GetDecisionHistoryUseCase getDecisionHistoryUseCase;
    private final AlgorithmDetailsLocalizer algorithmDetailsLocalizer;

    @PostMapping
    @Operation(summary = "Toma una nueva decisión", description = "Elige un ganador entre las opciones dadas usando el algoritmo indicado y persiste el resultado.")
    public MakeDecisionResponse makeDecision(@Valid @RequestBody MakeDecisionRequest request) {
        Objects.requireNonNull(request, "request");
        AlgorithmType type = parseAlgorithmType(request.getAlgorithmType());
        DecideCommand command = new DecideCommand(
                UserId.of(request.getUserId()),
                type,
                request.getOptions()
        );
        DecisionResult result = makeDecisionUseCase.decide(command);
        MakeDecisionResponse response = MakeDecisionResponse.fromResult(result);
        response.setAlgorithmDetails(algorithmDetailsLocalizer.localize(result.getAlgorithmDetails()));
        return response;
    }

    @GetMapping
    @Operation(summary = "Lista el historial de decisiones de un usuario")
    public List<MakeDecisionResponse> listByUser(@RequestParam("userId") Long userId) {
        List<Decision> decisions = getDecisionHistoryUseCase.listByUser(UserId.of(userId));
        return decisions.stream().map(decision -> {
            MakeDecisionResponse response = MakeDecisionResponse.fromDomain(decision);
            response.setAlgorithmDetails(algorithmDetailsLocalizer.localize(decision.getAlgorithmDetails()));
            return response;
        }).toList();
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
