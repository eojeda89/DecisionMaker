package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.adapter.in.web.dto.CreateTemplateRequest;
import com.eojeda89.decididorapi.adapter.in.web.dto.TemplateResponse;
import com.eojeda89.decididorapi.application.port.in.CreateTemplateUseCase;
import com.eojeda89.decididorapi.application.port.in.DeleteTemplateUseCase;
import com.eojeda89.decididorapi.application.port.in.GetTemplateUseCase;
import com.eojeda89.decididorapi.application.port.in.ListTemplatesUseCase;
import com.eojeda89.decididorapi.application.port.in.command.CreateTemplateCommand;
import com.eojeda89.decididorapi.application.port.out.UserRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions.ResourceNotFoundException;
import com.eojeda89.decididorapi.domain.model.TemplateId;
import com.eojeda89.decididorapi.domain.model.User;
import com.eojeda89.decididorapi.domain.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Tag(name = "Plantillas", description = "Sets de opciones reutilizables (Fase 3.6) — no deciden nada, solo evitan retipear las mismas opciones en cada decisión")
public class TemplateController {

    private final CreateTemplateUseCase createTemplateUseCase;
    private final ListTemplatesUseCase listTemplatesUseCase;
    private final GetTemplateUseCase getTemplateUseCase;
    private final DeleteTemplateUseCase deleteTemplateUseCase;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Crea una plantilla de opciones para el usuario autenticado")
    public TemplateResponse create(@Valid @RequestBody CreateTemplateRequest request) {
        Objects.requireNonNull(request, "request");
        CreateTemplateCommand command = CreateTemplateCommand.builder()
                .userId(resolveAuthenticatedUserId())
                .name(request.getName())
                .optionValues(request.getOptions())
                .build();
        return TemplateResponse.fromDomain(createTemplateUseCase.create(command));
    }

    @GetMapping
    @Operation(summary = "Lista las plantillas del usuario autenticado")
    public List<TemplateResponse> list() {
        return listTemplatesUseCase.listByUser(resolveAuthenticatedUserId()).stream()
                .map(TemplateResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una plantilla del usuario autenticado")
    public TemplateResponse get(@PathVariable Long id) {
        return TemplateResponse.fromDomain(getTemplateUseCase.get(resolveAuthenticatedUserId(), TemplateId.of(id)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borra una plantilla del usuario autenticado")
    public void delete(@PathVariable Long id) {
        deleteTemplateUseCase.delete(resolveAuthenticatedUserId(), TemplateId.of(id));
    }

    // Mismo criterio que DecisionController: el dueño se determina siempre
    // del JWT autenticado, nunca de un id que el cliente podría manipular.
    private UserId resolveAuthenticatedUserId() {
        String principalName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(principalName)
                .or(() -> userRepository.findByEmail(principalName))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        return user.getId();
    }
}
