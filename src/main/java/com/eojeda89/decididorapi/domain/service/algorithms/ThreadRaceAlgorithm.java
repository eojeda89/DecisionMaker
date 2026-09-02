package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

public class ThreadRaceAlgorithm implements DecisionAlgorithm {
    @Override
    public AlgorithmDetails chooseWinnerIndex(List<Option> options) {
        Objects.requireNonNull(options, "options");
        if (options.size() < 2) throw new Exceptions.InvalidRequestException("At least 2 options are required");
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
            Map<String, Object> details = Map.of(
                    "algorithm", "algorithm.thread-race.name",
                    "description", "algorithm.thread-race.description",
                    "custom_optionsCount", String.valueOf(options.size()),
                    "winnerIndex", first.get()
            );
            return AlgorithmDetails.of(details);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exceptions.AlgorithmFailureException("Interrupted while running the race", e);
        } catch (ExecutionException e) {
            throw new Exceptions.AlgorithmFailureException("Error executing race tasks", e.getCause());
        } finally {
            executor.shutdownNow();
        }
    }
}
