package com.eojeda89.decididorapi.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Igual que MakeDecisionRequest: sin userId, se resuelve del JWT.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BestOfNRequest {
    @NotNull(message = "rounds is required")
    private Integer rounds; // debe ser 3, 5 o 7 — validado en BestOfNDecisionService
    @NotNull
    @Size(min = 2, message = "At least 2 options are required")
    private List<@NotBlank String> options;
    // Opcional: de qué algoritmos sortea cada ronda (nombre de enum o code).
    // Si es null/vacío, sortea entre TODOS los algoritmos registrados.
    private List<String> algorithms;
}
