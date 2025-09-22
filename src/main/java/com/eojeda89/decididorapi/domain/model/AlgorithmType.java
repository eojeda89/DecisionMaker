package com.eojeda89.decididorapi.domain.model;

import lombok.Getter;

@Getter
public enum AlgorithmType {
    THREAD_RACE("thread-race", "Carrera de hilos"),
    DICE_ROLL("dice-roll", "Lanzamiento de dados"),
    FORTUNE_WHEEL("fortune-wheel", "Rueda de la fortuna"),
    RANDOM_WEIGHTED("random-weighted", "Aleatorio ponderado"),
    RANDOMIZED_VOTING("randomized-voting", "Votación aleatorizada"),
    FISHER_YATES_SELECTION("fisher-yates-selection", "Selección Fisher-Yates");

    private final String code;
    private final String uiName;

    AlgorithmType(String code, String uiName) {
        this.uiName = uiName;
        this.code = code;
    }

    public static AlgorithmType fromCode(String code) {
        for (AlgorithmType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown algorithm type: " + code);
    }

    public static AlgorithmType fromUiName(String uiName) {
        for (AlgorithmType type : values()) {
            if (type.getUiName().equals(uiName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown algorithm type: " + uiName);
    }
}
