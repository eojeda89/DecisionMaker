package com.eojeda89.decididorapi.application.port.in.result;

import com.eojeda89.decididorapi.domain.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DecisionResult {
    private DecisionId decisionId;
    private OptionId winningOptionId;
    private String winningOptionValue;
    private List<Option> options;
    private AlgorithmType algorithmType;
    private AlgorithmDetails algorithmDetails;
    private Instant createdAt;
    private String shareCode;
}
