package com.eojeda89.decididorapi.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Sin userId (se resuelve del JWT) ni algorithmType: "decisión del día"
// siempre usa la misma selección determinística por semilla.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyDecisionRequest {
    @NotNull
    @Size(min = 2, message = "At least 2 options are required")
    private List<@NotBlank String> options;
}
