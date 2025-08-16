package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class DiceRollAlgorithm implements DecisionAlgorithm {
    @Override
    public int chooseWinnerIndex(List<Option> options, AlgorithmDetails details) {
        Objects.requireNonNull(options, "options");
        if (options.size() < 2) throw new Exceptions.InvalidRequestException("At least 2 options are required");
        return ThreadLocalRandom.current().nextInt(options.size());
    }
}
