package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class RuletaFortunaAlgorithm implements DecisionAlgorithm {
    @Override
    public int chooseWinnerIndex(List<Option> options, AlgorithmDetails details) {
        Objects.requireNonNull(options, "options");
        if (options.size() < 2) throw new IllegalArgumentException("Se requieren al menos 2 opciones");
        int n = options.size();
        double angle = ThreadLocalRandom.current().nextDouble(360.0);
        double segment = 360.0 / n;
        int index = (int)Math.floor(angle / segment);
        if (index >= n) index = n - 1;
        return index;
    }
}
