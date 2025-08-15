package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.ThreadLocalRandom;

public class ThreadRaceAlgorithm implements DecisionAlgorithm {
    @Override
    public int chooseWinnerIndex(List<Option> options, AlgorithmDetails details) {
        Objects.requireNonNull(options, "options");
        if (options.size() < 2) throw new com.eojeda89.decididorapi.common.exception.Exceptions.InvalidRequestException("At least 2 options are required");
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
            throw new com.eojeda89.decididorapi.common.exception.Exceptions.AlgorithmFailureException("Interrupted while running the race", e);
        } catch (ExecutionException e) {
            throw new com.eojeda89.decididorapi.common.exception.Exceptions.AlgorithmFailureException("Error executing race tasks", e.getCause());
        } finally {
            executor.shutdownNow();
        }
    }
}
