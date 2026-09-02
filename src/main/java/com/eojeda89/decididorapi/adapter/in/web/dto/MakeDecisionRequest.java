package com.eojeda89.decididorapi.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// No lleva userId: quién decide se determina del JWT autenticado, nunca de
// un campo que el propio cliente podría manipular (ver DecisionController).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeDecisionRequest {
    @NotBlank
    private String algorithmType; // accepts enum name or code
    @NotNull
    @Size(min = 2, message = "At least 2 options are required")
    private List<@NotBlank String> options;
}
