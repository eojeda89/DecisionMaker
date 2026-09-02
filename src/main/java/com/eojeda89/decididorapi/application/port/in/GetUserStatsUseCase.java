package com.eojeda89.decididorapi.application.port.in;

import com.eojeda89.decididorapi.application.port.in.result.UserStatsResult;
import com.eojeda89.decididorapi.domain.model.UserId;

public interface GetUserStatsUseCase {
    UserStatsResult getStats(UserId userId);
}
