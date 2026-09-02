package com.eojeda89.decididorapi.application.port.in.command;

import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import com.eojeda89.decididorapi.domain.model.UserId;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BestOfNCommand {
    private UserId userId;
    private int rounds;
    @NotBlank(message = "Options cannot be empty")
    private List<String> optionValues;
    // Si es null/vacía, cada ronda sortea entre TODOS los algoritmos
    // registrados (ver BestOfNDecisionService).
    private List<AlgorithmType> algorithmPool;
}
