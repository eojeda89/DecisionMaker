package com.eojeda89.decididorapi.application.port.in;

import com.eojeda89.decididorapi.application.port.in.command.RegisterUserCommand;
import com.eojeda89.decididorapi.application.port.in.result.UserSummary;

public interface RegisterUserUseCase {
    UserSummary register(RegisterUserCommand command);
}
