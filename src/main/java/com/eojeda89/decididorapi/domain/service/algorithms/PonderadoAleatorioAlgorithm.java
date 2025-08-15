package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class PonderadoAleatorioAlgorithm implements DecisionAlgorithm {
    @Override
    public int chooseWinnerIndex(List<Option> options, AlgorithmDetails details) {
        Objects.requireNonNull(options, "options");
        if (options.size() < 2) throw new IllegalArgumentException("Se requieren al menos 2 opciones");
        int n = options.size();
        List<Integer> weights = new ArrayList<>(n);
        int total = 0;
        for (int i = 0; i < n; i++) {
            int w = ThreadLocalRandom.current().nextInt(1, 101); // 1..100
            weights.add(w);
            total += w;
        }
        int r = ThreadLocalRandom.current().nextInt(1, total + 1);
        for (int i = 0; i < n; i++) {
            r -= weights.get(i);
            if (r <= 0) return i;
        }
        return n - 1; // fallback teórico
    }
}
