package com.eojeda89.decididorapi.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeDecisionRequest {
    @NotNull
    private Long userId;
    @NotBlank
    private String algorithmType; // accepts enum name or code
    private Map<String, Object> algorithmDetails;
    @NotNull
    @Size(min = 2, message = "At least 2 options are required")
    private List<@NotBlank String> options;
}
