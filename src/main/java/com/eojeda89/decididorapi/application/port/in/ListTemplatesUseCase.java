package com.eojeda89.decididorapi.application.port.in;

import com.eojeda89.decididorapi.domain.model.OptionTemplate;
import com.eojeda89.decididorapi.domain.model.UserId;

import java.util.List;

public interface ListTemplatesUseCase {
    List<OptionTemplate> listByUser(UserId userId);
}
