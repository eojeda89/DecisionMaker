package com.eojeda89.decididorapi.application.port.in;

import com.eojeda89.decididorapi.application.port.in.command.CreateTemplateCommand;
import com.eojeda89.decididorapi.domain.model.OptionTemplate;

public interface CreateTemplateUseCase {
    OptionTemplate create(CreateTemplateCommand command);
}
