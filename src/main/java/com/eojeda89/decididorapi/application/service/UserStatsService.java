package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.GetUserStatsUseCase;
import com.eojeda89.decididorapi.application.port.in.result.UserStatsResult;
import com.eojeda89.decididorapi.application.port.out.DecisionRepository;
import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Fase 4.2: agregados sobre el historial completo del usuario (cuántas
 * decisiones tomó, qué algoritmo usa más, qué opción ganó más veces) para la
 * página de estadísticas personales. Solo lectura, no persiste nada.
 */
@Service
@RequiredArgsConstructor
public class UserStatsService implements GetUserStatsUseCase {

    private static final int TOP_WINNING_OPTIONS_LIMIT = 5;

    private final DecisionRepository decisionRepository;

    @Override
    public UserStatsResult getStats(UserId userId) {
        Objects.requireNonNull(userId, "userId");
        List<Decision> decisions = decisionRepository.findAllByUser(userId);

        // EnumMap (no HashMap): itera en el orden de declaración de
        // AlgorithmType, así que un empate en "más usado" siempre resuelve
        // igual entre corridas en vez de depender del hashing del enum.
        Map<AlgorithmType, Long> byAlgorithm = decisions.stream()
                .collect(Collectors.groupingBy(
                        Decision::getAlgorithmType,
                        () -> new EnumMap<>(AlgorithmType.class),
                        Collectors.counting()));

        AlgorithmType mostUsedAlgorithm = byAlgorithm.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // LinkedHashMap preservando el orden de "decisions" (más reciente
        // primero, ver findAllByUserWithOptions): un empate en victorias
        // queda desempatado por cuál ganó más recientemente.
        Map<String, Long> winCounts = decisions.stream()
                .map(Decision::getWinningOptionValue)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(v -> v, LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> topWinningOptions = winCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_WINNING_OPTIONS_LIMIT)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));

        Map.Entry<String, Long> mostWon = topWinningOptions.entrySet().stream().findFirst().orElse(null);

        return UserStatsResult.builder()
                .totalDecisions(decisions.size())
                .decisionsByAlgorithm(byAlgorithm)
                .mostUsedAlgorithm(mostUsedAlgorithm)
                .topWinningOptions(topWinningOptions)
                .mostWonOptionValue(mostWon != null ? mostWon.getKey() : null)
                .mostWonOptionCount(mostWon != null ? mostWon.getValue() : 0)
                .build();
    }
}
