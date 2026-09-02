package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Torneo eliminatorio aleatorio: las opciones se emparejan al azar y cada
 * ronda un duelo (cara o cruz, simulado internamente) elimina a una hasta
 * que queda una sola. Con cantidad impar de contendientes en una ronda, el
 * último (según el orden ya mezclado) pasa directo a la siguiente ronda
 * ("bye") sin pelear.
 * <p>
 * El mezclado inicial es sobre los ÍNDICES de las opciones, no sobre su
 * contenido, así que qué opción recibe un bye en una ronda dada también
 * queda determinado al azar — el resultado final sigue siendo uniforme
 * entre todas las opciones, con o sin byes (ver BracketTournamentAlgorithmTest).
 */
public class BracketTournamentAlgorithm implements DecisionAlgorithm {

    @Override
    public AlgorithmDetails chooseWinnerIndex(List<Option> options) {
        Objects.requireNonNull(options, "options");
        if (options.size() < 2) throw new Exceptions.InvalidRequestException("At least 2 options are required");

        List<Integer> contenders = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) contenders.add(i);
        Collections.shuffle(contenders, ThreadLocalRandom.current());

        List<Map<String, Object>> steps = new ArrayList<>();
        int round = 1;
        while (contenders.size() > 1) {
            List<Integer> nextRound = new ArrayList<>();
            for (int i = 0; i + 1 < contenders.size(); i += 2) {
                int a = contenders.get(i);
                int b = contenders.get(i + 1);
                int winner = ThreadLocalRandom.current().nextBoolean() ? a : b;
                steps.add(duelStep(round, options.get(a).getValue(), options.get(b).getValue(), options.get(winner).getValue()));
                nextRound.add(winner);
            }
            if (contenders.size() % 2 != 0) {
                int byeIndex = contenders.get(contenders.size() - 1);
                steps.add(byeStep(round, options.get(byeIndex).getValue()));
                nextRound.add(byeIndex);
            }
            contenders = nextRound;
            round++;
        }
        int winnerIndex = contenders.get(0);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("algorithm", "algorithm.bracket-tournament.name");
        details.put("description", "algorithm.bracket-tournament.description");
        details.put("custom_optionsCount", String.valueOf(options.size()));
        details.put("custom_rounds", String.valueOf(round - 1));
        details.put("steps", steps);
        details.put("winnerIndex", winnerIndex);
        return AlgorithmDetails.of(details);
    }

    private Map<String, Object> duelStep(int round, String a, String b, String winner) {
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("round", round);
        step.put("descriptionKey", "narrative.bracket.duel");
        // Arrays.asList (no List.of): tolera null, ver NarrativeSteps.
        step.put("args", Arrays.asList(round, a, b, winner));
        return step;
    }

    private Map<String, Object> byeStep(int round, String value) {
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("round", round);
        step.put("descriptionKey", "narrative.bracket.bye");
        step.put("args", Arrays.asList(round, value));
        return step;
    }
}
