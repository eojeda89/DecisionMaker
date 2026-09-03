package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.MakeBestOfNDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.command.BestOfNCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.application.port.out.DecisionRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.*;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Orquestador de "mejor de N" (Fase 3.2): corre N rondas independientes
 * sobre el mismo conjunto de opciones, cada una con un algoritmo elegido al
 * azar (de todo el registry, o de un subconjunto si el usuario lo pidió), y
 * gana la opción con más rondas ganadas (pluralidad simple). Si hay empate
 * en el primer puesto, se corre UNA ronda extra solo entre las opciones
 * empatadas para desempatar — siempre termina en esa ronda, porque
 * DecisionAlgorithm.chooseWinnerIndex() nunca devuelve un empate.
 * <p>
 * No es un DecisionAlgorithm en sí: orquesta los existentes vía el mismo
 * registry de DecisionAlgorithmConfig, así que no requiere tocarlos.
 */
@Service
@RequiredArgsConstructor
public class BestOfNDecisionService implements MakeBestOfNDecisionUseCase {

    private static final Set<Integer> VALID_ROUND_COUNTS = Set.of(3, 5, 7);

    private final DecisionRepository decisionRepository;
    private final Map<AlgorithmType, DecisionAlgorithm> algorithms;

    @Override
    public DecisionResult decide(BestOfNCommand command) {
        Objects.requireNonNull(command, "command");
        if (command.getOptionValues() == null || command.getOptionValues().size() < 2) {
            throw new Exceptions.InvalidRequestException("At least 2 options are required");
        }
        if (!VALID_ROUND_COUNTS.contains(command.getRounds())) {
            throw new Exceptions.InvalidRequestException("rounds must be 3, 5, or 7");
        }
        List<AlgorithmType> pool = resolvePool(command.getAlgorithmPool());

        List<Option> options = command.getOptionValues().stream()
                .map(v -> new Option(null, v))
                .toList();

        User user = new User();
        user.setId(command.getUserId());

        Decision decision = Decision.builder()
                .user(user)
                .algorithmType(AlgorithmType.BEST_OF_N)
                .options(options)
                .createdAt(Instant.now())
                .shareCode(ShareCodeGenerator.generate())
                .build();

        int[] winsByOptionIndex = new int[options.size()];
        List<Map<String, Object>> steps = new ArrayList<>();
        for (int round = 1; round <= command.getRounds(); round++) {
            AlgorithmType roundType = pickRandom(pool);
            int roundWinnerIndex = runAlgorithm(roundType, options);
            winsByOptionIndex[roundWinnerIndex]++;
            // El code (no el uiName, hardcodeado en español) para que
            // AlgorithmDetailsLocalizer lo resuelva en el idioma correcto.
            steps.add(step("narrative.best-of-n.round",
                    round, roundType.getCode(), options.get(roundWinnerIndex).getValue()));
        }

        int winnerIndex = resolveOverallWinner(winsByOptionIndex, options, pool, steps);
        steps.add(step("narrative.best-of-n.result",
                options.get(winnerIndex).getValue(), winsByOptionIndex[winnerIndex], command.getRounds()));

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("algorithm", "algorithm.best-of-n.name");
        details.put("description", "algorithm.best-of-n.description");
        details.put("custom_optionsCount", String.valueOf(options.size()));
        details.put("custom_rounds", String.valueOf(command.getRounds()));
        details.put("steps", steps);
        details.put("winnerIndex", winnerIndex);
        decision.setAlgorithmDetails(AlgorithmDetails.of(details));

        Decision persisted = decisionRepository.save(decision);
        OptionId winnerId = persisted.getOptions().get(winnerIndex).getId();
        persisted.selectWinner(winnerId);
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

    private List<AlgorithmType> resolvePool(List<AlgorithmType> requestedPool) {
        List<AlgorithmType> pool = (requestedPool == null || requestedPool.isEmpty())
                ? List.copyOf(algorithms.keySet())
                : requestedPool;
        for (AlgorithmType type : pool) {
            if (!algorithms.containsKey(type)) {
                throw new Exceptions.UnsupportedAlgorithmException("Unsupported algorithm: " + type);
            }
        }
        return pool;
    }

    private AlgorithmType pickRandom(List<AlgorithmType> pool) {
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    private int runAlgorithm(AlgorithmType type, List<Option> options) {
        AlgorithmDetails details = algorithms.get(type).chooseWinnerIndex(options);
        return details.get("winnerIndex", Integer.class);
    }

    // Pluralidad simple: gana quien tenga más rondas ganadas. Si hay empate
    // en el primer puesto, una única ronda extra SOLO entre las opciones
    // empatadas decide (siempre resuelve el empate, sin posibilidad de loop:
    // chooseWinnerIndex() sobre 2+ opciones siempre devuelve exactamente un
    // índice).
    private int resolveOverallWinner(int[] winsByOptionIndex, List<Option> options, List<AlgorithmType> pool,
                                      List<Map<String, Object>> steps) {
        int maxWins = Arrays.stream(winsByOptionIndex).max().orElseThrow();
        List<Integer> leaders = IntStream.range(0, winsByOptionIndex.length)
                .filter(i -> winsByOptionIndex[i] == maxWins)
                .boxed()
                .toList();
        if (leaders.size() == 1) {
            return leaders.get(0);
        }

        List<Option> tiedOptions = leaders.stream().map(options::get).toList();
        AlgorithmType tieBreakType = pickRandom(pool);
        int localWinnerIndex = runAlgorithm(tieBreakType, tiedOptions);
        int winnerIndex = leaders.get(localWinnerIndex);

        String tiedNames = tiedOptions.stream().map(Option::getValue).collect(Collectors.joining(", "));
        steps.add(step("narrative.best-of-n.tiebreak", tieBreakType.getCode(), tiedNames, options.get(winnerIndex).getValue()));
        return winnerIndex;
    }

    private Map<String, Object> step(String descriptionKey, Object... args) {
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("descriptionKey", descriptionKey);
        step.put("args", Arrays.asList(args));
        return step;
    }
}
