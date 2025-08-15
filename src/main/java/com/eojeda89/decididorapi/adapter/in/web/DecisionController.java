package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.application.port.in.GetDecisionHistoryUseCase;
import com.eojeda89.decididorapi.application.port.in.MakeDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.command.DecideCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.common.exception.Exceptions.InvalidRequestException;
import com.eojeda89.decididorapi.common.exception.Exceptions.UnsupportedAlgorithmException;
import com.eojeda89.decididorapi.domain.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        return decisions.stream().map(MakeDecisionResponse::fromDomain).collect(Collectors.toList());
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

    // ================== DTOs ==================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MakeDecisionRequest {
        @NotNull
        private Long userId;
        @NotBlank
        private String algorithmType; // accepts enum name or code
        private Map<String, Object> algorithmDetails;
        @NotNull
        @Size(min = 2, message = "At least 2 options are required")
        private List<@NotBlank String> options;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MakeDecisionResponse {
        private Long decisionId;
        private Long winningOptionId;
        private List<OptionDto> options;
        private String algorithmType; // code
        private Map<String, Object> algorithmDetails;
        private Instant createdAt;

        public static MakeDecisionResponse fromResult(DecisionResult result) {
            MakeDecisionResponse resp = new MakeDecisionResponse();
            resp.decisionId = result.getDecisionId() != null ? result.getDecisionId().value() : null;
            resp.winningOptionId = result.getWinningOptionId() != null ? result.getWinningOptionId().value() : null;
            resp.options = result.getOptions() != null ? result.getOptions().stream().map(OptionDto::fromDomain).collect(Collectors.toList()) : null;
            resp.algorithmType = result.getAlgorithmType() != null ? result.getAlgorithmType().getCode() : null;
            resp.algorithmDetails = result.getAlgorithmDetails() != null ? result.getAlgorithmDetails().getProperties() : null;
            resp.createdAt = result.getCreatedAt();
            return resp;
        }

        public static MakeDecisionResponse fromDomain(Decision decision) {
            MakeDecisionResponse resp = new MakeDecisionResponse();
            resp.decisionId = decision.getId() != null ? decision.getId().value() : null;
            resp.winningOptionId = decision.getWinningOptionId() != null ? decision.getWinningOptionId().value() : null;
            resp.options = decision.getOptions() != null ? decision.getOptions().stream().map(OptionDto::fromDomain).collect(Collectors.toList()) : null;
            resp.algorithmType = decision.getAlgorithmType() != null ? decision.getAlgorithmType().getCode() : null;
            resp.algorithmDetails = decision.getAlgorithmDetails() != null ? decision.getAlgorithmDetails().getProperties() : null;
            resp.createdAt = decision.getCreatedAt();
            return resp;
        }

        public Long getDecisionId() { return decisionId; }
        public Long getWinningOptionId() { return winningOptionId; }
        public List<OptionDto> getOptions() { return options; }
        public String getAlgorithmType() { return algorithmType; }
        public Map<String, Object> getAlgorithmDetails() { return algorithmDetails; }
        public Instant getCreatedAt() { return createdAt; }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDto {
        private Long id;
        private String value;

        public static OptionDto fromDomain(Option o) {
            OptionDto dto = new OptionDto();
            dto.id = o.getId() != null ? o.getId().value() : null;
            dto.value = o.getValue();
            return dto;
        }
    }
}
