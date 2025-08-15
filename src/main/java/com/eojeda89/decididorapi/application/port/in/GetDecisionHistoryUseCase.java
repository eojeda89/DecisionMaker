package com.eojeda89.decididorapi.application.port.in;

import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.UserId;

import java.util.List;

public interface GetDecisionHistoryUseCase {
    List<Decision> listByUser(UserId userId);
}
