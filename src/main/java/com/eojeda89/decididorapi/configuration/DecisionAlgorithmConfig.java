package com.eojeda89.decididorapi.configuration;

import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;
import com.eojeda89.decididorapi.domain.service.algorithms.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class DecisionAlgorithmConfig {

    @Bean(name = "threadRaceAlgorithm")
    public DecisionAlgorithm threadRaceAlgorithm() { return new ThreadRaceAlgorithm(); }

    @Bean(name = "diceRollAlgorithm")
    public DecisionAlgorithm diceRollAlgorithm() { return new DiceRollAlgorithm(); }

    @Bean(name = "fortuneWheelAlgorithm")
    public DecisionAlgorithm fortuneWheelAlgorithm() { return new FortuneWheelAlgorithm(); }

    @Bean(name = "randomWeightedAlgorithm")
    public DecisionAlgorithm randomWeightedAlgorithm() { return new RandomWeightedAlgorithm(); }

    @Bean(name = "randomizedVotingAlgorithm")
    public DecisionAlgorithm randomizedVotingAlgorithm() {
        return new RandomizedVoting();
    }

    @Bean(name = "fisherYatesSelectionAlgorithm")
    public DecisionAlgorithm fisherYatesSelectionAlgorithm() {
        return new FisherYatesSelection();
    }

    @Bean
    public Map<AlgorithmType, DecisionAlgorithm> decisionAlgorithms(
            @Qualifier("threadRaceAlgorithm") DecisionAlgorithm threadRace,
            @Qualifier("diceRollAlgorithm") DecisionAlgorithm diceRoll,
            @Qualifier("fortuneWheelAlgorithm") DecisionAlgorithm fortuneWheel,
            @Qualifier("randomWeightedAlgorithm") DecisionAlgorithm randomWeighted,
            @Qualifier("randomizedVotingAlgorithm") DecisionAlgorithm randomizedVoting,
            @Qualifier("fisherYatesSelectionAlgorithm") DecisionAlgorithm fisherYatesSelection
    ) {
        Map<AlgorithmType, DecisionAlgorithm> map = new EnumMap<>(AlgorithmType.class);
        map.put(AlgorithmType.THREAD_RACE, threadRace);
        map.put(AlgorithmType.DICE_ROLL, diceRoll);
        map.put(AlgorithmType.FORTUNE_WHEEL, fortuneWheel);
        map.put(AlgorithmType.RANDOM_WEIGHTED, randomWeighted);
        map.put(AlgorithmType.RANDOMIZED_VOTING, randomizedVoting);
        map.put(AlgorithmType.FISHER_YATES_SELECTION, fisherYatesSelection);
        return map;
    }
}
