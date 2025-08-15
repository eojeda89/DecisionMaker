package com.eojeda89.decididorapi.application.port.in;

import com.eojeda89.decididorapi.application.port.in.command.DecideCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;

public interface MakeDecisionUseCase {
    DecisionResult decide(DecideCommand command);
}
