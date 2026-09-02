package com.eojeda89.decididorapi.application.port.out;

import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DecisionRepository {
    Decision save(Decision decision);
    Page<Decision> findByUser(UserId userId, Pageable pageable);
}
