package com.eojeda89.decididorapi.application.port.out;

import com.eojeda89.decididorapi.domain.model.OptionTemplate;
import com.eojeda89.decididorapi.domain.model.TemplateId;
import com.eojeda89.decididorapi.domain.model.UserId;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository {
    OptionTemplate save(OptionTemplate template);
    List<OptionTemplate> findByUser(UserId userId);
    Optional<OptionTemplate> findById(TemplateId id);
    void deleteById(TemplateId id);
}
