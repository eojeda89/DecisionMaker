package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class DiceRollAlgorithm implements DecisionAlgorithm {
    @Override
    public AlgorithmDetails chooseWinnerIndex(List<Option> options) {
        Objects.requireNonNull(options, "options");
        if (options.size() < 2) throw new Exceptions.InvalidRequestException("At least 2 options are required");
        int winnerIndex = ThreadLocalRandom.current().nextInt(options.size());
        Map<String, Object> details = Map.of(
                "algorithm", "algorithm.dice-roll.name",
                "description", "algorithm.dice-roll.description",
                "custom_optionsCount", String.valueOf(options.size()),
                "steps", NarrativeSteps.singleStep("narrative.dice-roll", options.size(), options.get(winnerIndex).getValue()),
                "winnerIndex", winnerIndex
        );
        return AlgorithmDetails.of(details);
    }
}
