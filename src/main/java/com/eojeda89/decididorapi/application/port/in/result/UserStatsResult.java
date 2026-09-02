package com.eojeda89.decididorapi.application.port.in.result;

import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStatsResult {
    private long totalDecisions;
    private Map<AlgorithmType, Long> decisionsByAlgorithm;
    private AlgorithmType mostUsedAlgorithm;
    // Orden descendente por cantidad de victorias, ya recortado al top N.
    private Map<String, Long> topWinningOptions;
    private String mostWonOptionValue;
    private long mostWonOptionCount;
}
