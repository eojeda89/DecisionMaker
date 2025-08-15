package com.eojeda89.decididorapi.application.port.out;

import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.UserId;

import java.util.List;

public interface DecisionRepository {
    Decision save(Decision decision);
    List<Decision> findByUser(UserId userId);
}
