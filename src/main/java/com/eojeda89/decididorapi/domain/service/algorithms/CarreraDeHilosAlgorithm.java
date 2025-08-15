package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.ThreadLocalRandom;

public class CarreraDeHilosAlgorithm implements DecisionAlgorithm {
    @Override
    public int chooseWinnerIndex(List<Option> options, AlgorithmDetails details) {
        Objects.requireNonNull(options, "options");
        if (options.size() < 2) throw new IllegalArgumentException("Se requieren al menos 2 opciones");
        int n = options.size();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(n, Runtime.getRuntime().availableProcessors()));
        CompletionService<Integer> cs = new ExecutorCompletionService<>(executor);
        try {
            for (int i = 0; i < n; i++) {
                final int idx = i;
                cs.submit(() -> {
                    long sleep = ThreadLocalRandom.current().nextLong(100, 1001);
                    try { Thread.sleep(sleep); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    return idx;
                });
            }
            Future<Integer> first = cs.take();
            return first.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrumpido mientras se ejecutaba la carrera", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error ejecutando tareas de la carrera", e.getCause());
        } finally {
            executor.shutdownNow();
        }
    }
}
