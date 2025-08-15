package com.eojeda89.decididorapi.configuration;

import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;
import com.eojeda89.decididorapi.domain.service.algorithms.ThreadRaceAlgorithm;
import com.eojeda89.decididorapi.domain.service.algorithms.DiceRollAlgorithm;
import com.eojeda89.decididorapi.domain.service.algorithms.RandomWeightedAlgorithm;
import com.eojeda89.decididorapi.domain.service.algorithms.FortuneWheelAlgorithm;
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

    @Bean
    public Map<AlgorithmType, DecisionAlgorithm> decisionAlgorithms(
            @Qualifier("threadRaceAlgorithm") DecisionAlgorithm threadRace,
            @Qualifier("diceRollAlgorithm") DecisionAlgorithm diceRoll,
            @Qualifier("fortuneWheelAlgorithm") DecisionAlgorithm fortuneWheel,
            @Qualifier("randomWeightedAlgorithm") DecisionAlgorithm randomWeighted
    ) {
        Map<AlgorithmType, DecisionAlgorithm> map = new EnumMap<>(AlgorithmType.class);
        map.put(AlgorithmType.THREAD_RACE, threadRace);
        map.put(AlgorithmType.DICE_ROLL, diceRoll);
        map.put(AlgorithmType.FORTUNE_WHEEL, fortuneWheel);
        map.put(AlgorithmType.RANDOM_WEIGHTED, randomWeighted);
        return map;
    }
}
