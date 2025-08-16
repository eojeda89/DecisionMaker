package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.adapter.in.web.dto.MakeDecisionRequest;
import com.eojeda89.decididorapi.adapter.in.web.dto.MakeDecisionResponse;
import com.eojeda89.decididorapi.application.port.in.GetDecisionHistoryUseCase;
import com.eojeda89.decididorapi.application.port.in.MakeDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.command.DecideCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.common.exception.Exceptions.InvalidRequestException;
import com.eojeda89.decididorapi.common.exception.Exceptions.UnsupportedAlgorithmException;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.UserId;
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
public class DecisionController {

    private final MakeDecisionUseCase makeDecisionUseCase;
    private final GetDecisionHistoryUseCase getDecisionHistoryUseCase;

    @PostMapping
    public MakeDecisionResponse makeDecision(@Valid @RequestBody MakeDecisionRequest request) {
        Objects.requireNonNull(request, "request");
        AlgorithmType type = parseAlgorithmType(request.getAlgorithmType());
        DecideCommand command = new DecideCommand(
                UserId.of(request.getUserId()),
                type,
                AlgorithmDetails.of(request.getAlgorithmDetails()),
                request.getOptions()
        );
        DecisionResult result = makeDecisionUseCase.decide(command);
        return MakeDecisionResponse.fromResult(result);
    }

    @GetMapping
    public List<MakeDecisionResponse> listByUser(@RequestParam("userId") Long userId) {
        List<Decision> decisions = getDecisionHistoryUseCase.listByUser(UserId.of(userId));
        return decisions.stream().map(MakeDecisionResponse::fromDomain).toList();
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
