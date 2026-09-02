package com.eojeda89.decididorapi.application.port.in;

import com.eojeda89.decididorapi.application.port.in.command.BestOfNCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;

public interface MakeBestOfNDecisionUseCase {
    DecisionResult decide(BestOfNCommand command);
}
