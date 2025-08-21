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
public class DecideCommand {
    private UserId userId;
    private AlgorithmType algorithmType;
    @NotBlank(message = "Options cannot be empty")
    private List<String> optionValues;
}
