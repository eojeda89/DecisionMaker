package com.eojeda89.decididorapi.domain.model;

import lombok.Getter;

@Getter
public enum AlgorithmType {
    THREAD_RACE("thread-race"),
    DICE_ROLL("dice-roll"),
    FORTUNE_WHEEL("fortune-wheel"),
    RANDOM_WEIGHTED("random-weighted");

    private final String code;

    AlgorithmType(String code) {
        this.code = code;
    }

}
