package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class RandomizedVoting implements DecisionAlgorithm {

    @Override
    public AlgorithmDetails chooseWinnerIndex(List<Option> options) {
        Objects.requireNonNull(options, "options");
        if (options.size() < 2) throw new Exceptions.InvalidRequestException("At least 2 options are required");
        Map<Integer, Integer> votes = new HashMap<>();
        for (int i = 0; i < options.size(); i++) {
            votes.put(i, ThreadLocalRandom.current().nextInt(100)); // Votos aleatorios
        }
        int winnerIndex = votes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);

        Map<String, Object> details = Map.of(
                "algorithm", "algorithm.randomized-voting.name",
                "description", "algorithm.randomized-voting.description",
                "custom_optionsCount", String.valueOf(options.size()),
                "custom_votes", votes.values(),
                "winnerIndex", winnerIndex
        );
        return AlgorithmDetails.of(details);
    }
}
