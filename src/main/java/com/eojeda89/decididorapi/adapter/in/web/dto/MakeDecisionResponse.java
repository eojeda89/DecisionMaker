package com.eojeda89.decididorapi.adapter.in.web.dto;

import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.domain.model.Decision;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeDecisionResponse {
    private Long decisionId;
    private Long winningOptionId;
    private List<OptionDto> options;
    private String algorithmType; // code
    private Map<String, Object> algorithmDetails;
    private Instant createdAt;
    // Código para GET /api/decisions/shared/{shareCode} (sin login). No es
    // información sensible: a propósito no lleva ningún dato del dueño.
    private String shareCode;

    public static MakeDecisionResponse fromResult(DecisionResult result) {
        MakeDecisionResponse resp = new MakeDecisionResponse();
        resp.decisionId = result.getDecisionId() != null ? result.getDecisionId().value() : null;
        resp.winningOptionId = result.getWinningOptionId() != null ? result.getWinningOptionId().value() : null;
        resp.options = result.getOptions() != null ? result.getOptions().stream().map(OptionDto::fromDomain).toList() : null;
        resp.algorithmType = result.getAlgorithmType() != null ? result.getAlgorithmType().getCode() : null;
        resp.algorithmDetails = result.getAlgorithmDetails() != null ? result.getAlgorithmDetails().getProperties() : null;
        resp.createdAt = result.getCreatedAt();
        resp.shareCode = result.getShareCode();
        return resp;
    }

    public static MakeDecisionResponse fromDomain(Decision decision) {
        MakeDecisionResponse resp = new MakeDecisionResponse();
        resp.decisionId = decision.getId() != null ? decision.getId().value() : null;
        resp.winningOptionId = decision.getWinningOptionId() != null ? decision.getWinningOptionId().value() : null;
        resp.options = decision.getOptions() != null ? decision.getOptions().stream().map(OptionDto::fromDomain).toList() : null;
        resp.algorithmType = decision.getAlgorithmType() != null ? decision.getAlgorithmType().getCode() : null;
        resp.algorithmDetails = decision.getAlgorithmDetails() != null ? decision.getAlgorithmDetails().getProperties() : null;
        resp.createdAt = decision.getCreatedAt();
        resp.shareCode = decision.getShareCode();
        return resp;
    }
}
