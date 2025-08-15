package com.eojeda89.decididorapi.domain.model;

import lombok.Getter;

@Getter
public enum AlgorithmType {
    CARRERA_HILOS("carrera-hilos"),
    LANZAMIENTO_DADO("lanzamiento-dado"),
    RULETA_FORTUNA("ruleta-fortuna"),
    PONDERADO_ALEATORIO("ponderado-aleatorio");

    private final String nombre;

    AlgorithmType(String nombre) {
        this.nombre = nombre;
    }

}
