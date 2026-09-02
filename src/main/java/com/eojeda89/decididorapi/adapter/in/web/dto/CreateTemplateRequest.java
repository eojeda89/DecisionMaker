package com.eojeda89.decididorapi.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Sin userId: el dueño se determina del JWT autenticado (ver TemplateController).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateRequest {
    @NotBlank
    private String name;
    @NotNull
    @Size(min = 2, message = "At least 2 options are required")
    private List<@NotBlank String> options;
}
