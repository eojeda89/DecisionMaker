package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.GetDecisionHistoryUseCase;
import com.eojeda89.decididorapi.application.port.in.GetSharedDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.MakeDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.command.DecideCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.application.port.out.DecisionRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.*;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DecisionService implements MakeDecisionUseCase, GetDecisionHistoryUseCase, GetSharedDecisionUseCase {

    // Fase 3.4 (anti-repetición): cuántas decisiones recientes del usuario se
    // escanean buscando la misma lista de opciones, cuántas de esas cuentan
    // para el "recentWinCount", y el tope de re-tiradas para no loopear.
    private static final int HISTORY_SCAN_SIZE = 20;
    private static final int RECENT_WINDOW = 5;
    private static final int MAX_REROLL_ATTEMPTS = 4;

    private final DecisionRepository decisionRepository;
    private final Map<AlgorithmType, DecisionAlgorithm> algorithms;

    @Override
    public DecisionResult decide(DecideCommand command) {
        Objects.requireNonNull(command, "command");
        if (command.getOptionValues() == null || command.getOptionValues().size() < 2) {
            throw new Exceptions.InvalidRequestException("At least 2 options are required");
        }
        if (command.getAlgorithmType() == null) {
            throw new Exceptions.InvalidRequestException("algorithmType is required");
        }
        DecisionAlgorithm algorithm = algorithms.get(command.getAlgorithmType());
        if (algorithm == null) {
            throw new Exceptions.UnsupportedAlgorithmException("Unsupported algorithm: " + command.getAlgorithmType());
        }

        // Construir opciones de dominio (ids aún no asignadas)
        List<Option> options = command.getOptionValues().stream()
                .map(v -> new Option(null, v))
                .toList();

        // Construir usuario (por ahora solo id)
        User user = new User();
        user.setId(command.getUserId());

        // Crear agregado Decision sin ganador todavía y sin detalles de algoritmo
        Decision decision = Decision.builder()
                .user(user)
                .algorithmType(command.getAlgorithmType())
                .options(options)
                .createdAt(Instant.now())
                .shareCode(ShareCodeGenerator.generate())
                .build();

        // Elegir ganador por índice (sin depender de ids aún)
        AlgorithmDetails algorithmDetails = command.isAvoidRepeats()
                ? chooseWinnerAvoidingRepeats(algorithm, options, command.getUserId(), command.getOptionValues())
                : algorithm.chooseWinnerIndex(options);
        decision.setAlgorithmDetails(algorithmDetails);
        if (algorithmDetails == null) {
            throw new Exceptions.InvalidRequestException("Algorithm did not return valid details");
        }
        int winnerIndex = algorithmDetails.get("winnerIndex", Integer.class);
        if (winnerIndex < 0 || winnerIndex >= options.size()) {
            throw new Exceptions.DomainValidationException("Algorithm produced an out-of-range index");
        }

        // Persistir para obtener ids
        Decision persisted = decisionRepository.save(decision);
        OptionId winnerId = persisted.getOptions().get(winnerIndex).getId();
        persisted.selectWinner(winnerId);

        // Guardar el ganador
        Decision finalized = decisionRepository.save(persisted);

        return DecisionResult.builder()
                .decisionId(finalized.getId())
                .winningOptionId(finalized.getWinningOptionId())
                .winningOptionValue(finalized.getWinningOptionValue())
                .options(finalized.getOptions())
                .algorithmType(finalized.getAlgorithmType())
                .algorithmDetails(finalized.getAlgorithmDetails())
                .createdAt(finalized.getCreatedAt())
                .shareCode(finalized.getShareCode())
                .build();
    }

    @Override
    public Page<Decision> listByUser(UserId userId, Pageable pageable) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(pageable, "pageable");
        return decisionRepository.findByUser(userId, pageable);
    }

    @Override
    public Decision getByShareCode(String shareCode) {
        Objects.requireNonNull(shareCode, "shareCode");
        return decisionRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Shared decision not found"));
    }

    // Corre el algoritmo normalmente; si el ganador ya ganó recientemente
    // ESTA MISMA lista de opciones, con una probabilidad creciente según
    // cuántas veces ganó, se descarta el resultado y se vuelve a tirar
    // (nunca se fuerza un resultado distinto a mano: sigue siendo el
    // algoritmo el que decide en cada intento). Tope de MAX_REROLL_ATTEMPTS
    // re-tiradas para no loopear -- con probabilidad < 100% no hay garantía
    // matemática de corte, así que el tope es necesario igual.
    private AlgorithmDetails chooseWinnerAvoidingRepeats(DecisionAlgorithm algorithm, List<Option> options,
                                                          UserId userId, List<String> optionValues) {
        List<Decision> recentMatches = recentDecisionsWithSameOptions(userId, optionValues);
        List<Map<String, Object>> rerollSteps = new ArrayList<>();

        AlgorithmDetails details = algorithm.chooseWinnerIndex(options);
        for (int attempt = 1; attempt <= MAX_REROLL_ATTEMPTS; attempt++) {
            String candidateValue = options.get(details.get("winnerIndex", Integer.class)).getValue();
            long recentWinCount = recentMatches.stream()
                    .filter(d -> candidateValue.equals(d.getWinningOptionValue()))
                    .count();
            if (recentWinCount == 0) break;

            double rerollProbability = Math.min(0.33 * recentWinCount, 0.9);
            if (ThreadLocalRandom.current().nextDouble() >= rerollProbability) break;

            rerollSteps.add(rerollStep(candidateValue, recentWinCount, recentMatches.size(), attempt));
            details = algorithm.chooseWinnerIndex(options);
        }

        if (rerollSteps.isEmpty()) return details;
        return prependSteps(details, rerollSteps);
    }

    // Últimas RECENT_WINDOW decisiones del usuario cuya lista de opciones
    // (como conjunto, sin importar orden) es EXACTAMENTE la misma que la
    // actual -- sin importar qué algoritmo se usó. findByUser ya ordena por
    // más reciente primero.
    private List<Decision> recentDecisionsWithSameOptions(UserId userId, List<String> optionValues) {
        Set<String> currentSet = new HashSet<>(optionValues);
        Page<Decision> recent = decisionRepository.findByUser(userId, PageRequest.of(0, HISTORY_SCAN_SIZE));
        return recent.getContent().stream()
                .filter(d -> d.getOptions() != null
                        && currentSet.equals(d.getOptions().stream().map(Option::getValue).collect(Collectors.toSet())))
                .limit(RECENT_WINDOW)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private AlgorithmDetails prependSteps(AlgorithmDetails details, List<Map<String, Object>> rerollSteps) {
        Map<String, Object> merged = new LinkedHashMap<>(details.getProperties());
        List<Map<String, Object>> combined = new ArrayList<>(rerollSteps);
        if (merged.get("steps") instanceof List<?> existing) {
            combined.addAll((List<Map<String, Object>>) existing);
        }
        merged.put("steps", combined);
        return AlgorithmDetails.of(merged);
    }

    private Map<String, Object> rerollStep(String candidateValue, long recentWinCount, int matchedHistorySize, int attempt) {
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("descriptionKey", "narrative.anti-repeat.reroll");
        step.put("args", Arrays.asList(candidateValue, recentWinCount, matchedHistorySize, attempt));
        return step;
    }
}
