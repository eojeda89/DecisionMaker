package com.eojeda89.decididorapi.application.port.in.command;

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
public class CreateTemplateCommand {
    private UserId userId;
    @NotBlank(message = "Name cannot be empty")
    private String name;
    private List<String> optionValues;
}
