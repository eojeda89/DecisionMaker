package com.eojeda89.decididorapi.application.port.in;

import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetDecisionHistoryUseCase {
    Page<Decision> listByUser(UserId userId, Pageable pageable);
}
