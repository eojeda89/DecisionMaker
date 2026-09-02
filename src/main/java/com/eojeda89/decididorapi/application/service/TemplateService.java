package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.CreateTemplateUseCase;
import com.eojeda89.decididorapi.application.port.in.DeleteTemplateUseCase;
import com.eojeda89.decididorapi.application.port.in.GetTemplateUseCase;
import com.eojeda89.decididorapi.application.port.in.ListTemplatesUseCase;
import com.eojeda89.decididorapi.application.port.in.command.CreateTemplateCommand;
import com.eojeda89.decididorapi.application.port.out.TemplateRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.OptionTemplate;
import com.eojeda89.decididorapi.domain.model.TemplateId;
import com.eojeda89.decididorapi.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Plantillas de opciones reutilizables (Fase 3.6): guardar un set de
 * opciones frecuentes para no volver a tipearlas en cada decisión. No
 * decide nada -- es un CRUD acotado al dueño de cada plantilla.
 */
@Service
@RequiredArgsConstructor
public class TemplateService implements CreateTemplateUseCase, ListTemplatesUseCase, GetTemplateUseCase, DeleteTemplateUseCase {

    private final TemplateRepository templateRepository;

    @Override
    public OptionTemplate create(CreateTemplateCommand command) {
        Objects.requireNonNull(command, "command");
        if (command.getOptionValues() == null || command.getOptionValues().size() < 2) {
            throw new Exceptions.InvalidRequestException("At least 2 options are required");
        }
        OptionTemplate template = OptionTemplate.builder()
                .userId(command.getUserId())
                .name(command.getName())
                .optionValues(command.getOptionValues())
                .createdAt(Instant.now())
                .build();
        return templateRepository.save(template);
    }

    @Override
    public List<OptionTemplate> listByUser(UserId userId) {
        Objects.requireNonNull(userId, "userId");
        return templateRepository.findByUser(userId);
    }

    @Override
    public OptionTemplate get(UserId userId, TemplateId templateId) {
        return findOwned(userId, templateId);
    }

    @Override
    public void delete(UserId userId, TemplateId templateId) {
        OptionTemplate template = findOwned(userId, templateId);
        templateRepository.deleteById(template.getId());
    }

    // No es del usuario -> mismo mensaje/status que "no existe", para no
    // filtrar si un id de otro usuario existe (mismo patrón que
    // resolveAuthenticatedUserId en los controllers: nunca confiar en
    // ids ajenos que el cliente podría adivinar).
    private OptionTemplate findOwned(UserId userId, TemplateId templateId) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(templateId, "templateId");
        OptionTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Template not found"));
        if (!userId.equals(template.getUserId())) {
            throw new Exceptions.ResourceNotFoundException("Template not found");
        }
        return template;
    }
}
