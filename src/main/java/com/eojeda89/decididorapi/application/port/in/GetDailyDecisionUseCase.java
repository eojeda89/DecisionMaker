package com.eojeda89.decididorapi.application.port.in;

import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.domain.model.UserId;

import java.util.List;

public interface GetDailyDecisionUseCase {
    // Determinístico: mismo (día UTC, userId, opciones) siempre da el mismo
    // resultado. No persiste nada -- se recalcula en cada llamada.
    DecisionResult getDaily(UserId userId, List<String> optionValues);
}
