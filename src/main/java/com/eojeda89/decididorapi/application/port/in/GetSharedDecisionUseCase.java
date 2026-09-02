package com.eojeda89.decididorapi.application.port.in;

import com.eojeda89.decididorapi.domain.model.Decision;

public interface GetSharedDecisionUseCase {
    // Lanza Exceptions.ResourceNotFoundException si el código no existe.
    Decision getByShareCode(String shareCode);
}
