package com.eojeda89.decididorapi.domain.model;

import lombok.Getter;

@Getter
public enum AlgorithmType {
    THREAD_RACE("thread-race", "Carrera de hilos"),
    DICE_ROLL("dice-roll", "Lanzamiento de dados"),
    FORTUNE_WHEEL("fortune-wheel", "Rueda de la fortuna"),
    RANDOM_WEIGHTED("random-weighted", "Aleatorio ponderado"),
    RANDOMIZED_VOTING("randomized-voting", "Votación aleatorizada"),
    FISHER_YATES_SELECTION("fisher-yates-selection", "Selección Fisher-Yates"),
    BRACKET_TOURNAMENT("bracket-tournament", "Torneo eliminatorio"),
    // Meta-algoritmo (Fase 3.2): orquesta N corridas de los algoritmos de
    // arriba vía BestOfNDecisionService, no un DecisionAlgorithm propio, así
    // que no tiene entrada en el mapa de DecisionAlgorithmConfig. Se excluye
    // a propósito del dropdown de /form (AppController) porque no encaja en
    // el flujo de "un solo algoritmo" de esa pantalla.
    BEST_OF_N("best-of-n", "Mejor de N"),
    // Fase 3.5: resultado determinístico (semilla = fecha + usuario +
    // opciones) vía DailyDecisionService, no persistido, no un
    // DecisionAlgorithm propio. También excluido del dropdown de /form.
    DAILY("daily", "Decisión del día");

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
