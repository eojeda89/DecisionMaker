package com.eojeda89.decididorapi.application.port.in;

import com.eojeda89.decididorapi.domain.model.OptionTemplate;
import com.eojeda89.decididorapi.domain.model.TemplateId;
import com.eojeda89.decididorapi.domain.model.UserId;

public interface GetTemplateUseCase {
    // Lanza Exceptions.ResourceNotFoundException si no existe o no es del
    // usuario (mismo mensaje en ambos casos, para no filtrar si existe).
    OptionTemplate get(UserId userId, TemplateId templateId);
}
