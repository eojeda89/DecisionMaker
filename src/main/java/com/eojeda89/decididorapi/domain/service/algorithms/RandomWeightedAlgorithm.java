package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class RandomWeightedAlgorithm implements DecisionAlgorithm {
    @Override
    public AlgorithmDetails chooseWinnerIndex(List<Option> options) {
        Objects.requireNonNull(options, "options");
        if (options.size() < 2) throw new Exceptions.InvalidRequestException("At least 2 options are required");
        int n = options.size();
        List<Integer> weights = new ArrayList<>(n);
        int total = 0;
        int winnerIndex = -1;
        for (int i = 0; i < n; i++) {
            int w = ThreadLocalRandom.current().nextInt(1, 101);
            weights.add(w);
            total += w;
        }
        int r = ThreadLocalRandom.current().nextInt(1, total + 1);
        for (int i = 0; i < n; i++) {
            r -= weights.get(i);
            if (r <= 0) {
                winnerIndex = i;
                break;
            }
        }

        Map<String, Object> details = Map.of(
                "algorithm", "algorithm.random-weighted.name",
                "description", "algorithm.random-weighted.description",
                "custom_optionsCount", String.valueOf(options.size()),
                "custom_weights", weights.toString(),
                "winnerIndex", winnerIndex
        );
        return AlgorithmDetails.of(details);
    }
}
