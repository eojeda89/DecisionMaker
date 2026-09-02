package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class FisherYatesSelection implements DecisionAlgorithm {

    private final Random random = SecureRandom.getInstanceStrong();

    public FisherYatesSelection() throws NoSuchAlgorithmException {
        // Constructor to ensure SecureRandom is initialized
    }

    @Override
    public AlgorithmDetails chooseWinnerIndex(List<Option> options) {
        Objects.requireNonNull(options, "options");
        if (options.size() < 2) throw new Exceptions.InvalidRequestException("At least 2 options are required");
        int winnerIndex = -1;
        // Fisher-Yates shuffle to randomly select a winner
        List<Option> shuffledList = new ArrayList<>(options);
        Collections.shuffle(shuffledList, random);
        winnerIndex = options.indexOf(shuffledList.getFirst());
        List<String> shuffledOptions = shuffledList.stream()
                .map(Option::getValue)
                .toList();
        Map<String, Object> details = Map.of(
                "algorithm", "Selección Fisher-Yates",
                "description", "Se elige un ganador al azar barajando la lista de opciones y tomando el primer elemento.",
                "custom_optionsCount", String.valueOf(options.size()),
                "custom_shuffledOptions", shuffledOptions,
                "winnerIndex", winnerIndex
        );
        return AlgorithmDetails.of(details);
    }
}