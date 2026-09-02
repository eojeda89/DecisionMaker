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
import com.eojeda89.decididorapi.domain.model.OptionTemplate;
import com.eojeda89.decididorapi.domain.model.TemplateId;
import com.eojeda89.decididorapi.domain.model.User;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TemplateControllerTest {

    @Mock
    private CreateTemplateUseCase createTemplateUseCase;
    @Mock
    private ListTemplatesUseCase listTemplatesUseCase;
    @Mock
    private GetTemplateUseCase getTemplateUseCase;
    @Mock
    private DeleteTemplateUseCase deleteTemplateUseCase;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TemplateController controller;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("authenticatedUser", null, List.of())
        );
        lenient().when(userRepository.findByUsername("authenticatedUser"))
                .thenReturn(Optional.of(User.builder().id(UserId.of(1L)).username("authenticatedUser").build()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_HappyPath() {
        CreateTemplateRequest request = new CreateTemplateRequest("Restaurantes", List.of("A", "B"));
        OptionTemplate saved = OptionTemplate.builder().id(TemplateId.of(10L)).userId(UserId.of(1L))
                .name("Restaurantes").optionValues(List.of("A", "B")).build();
        when(createTemplateUseCase.create(any(CreateTemplateCommand.class))).thenReturn(saved);

        TemplateResponse response = controller.create(request);

        assertEquals(10L, response.getId());
        assertEquals("Restaurantes", response.getName());
        verify(createTemplateUseCase).create(argThat(cmd -> cmd.getUserId().equals(UserId.of(1L))));
    }

    @Test
    void create_NullRequest_ThrowsException() {
        assertThrows(NullPointerException.class, () -> controller.create(null));
    }

    @Test
    void list_HappyPath() {
        OptionTemplate t1 = OptionTemplate.builder().id(TemplateId.of(1L)).name("A").optionValues(List.of("X", "Y")).build();
        when(listTemplatesUseCase.listByUser(UserId.of(1L))).thenReturn(List.of(t1));

        List<TemplateResponse> result = controller.list();

        assertEquals(1, result.size());
        verify(listTemplatesUseCase).listByUser(UserId.of(1L));
    }

    @Test
    void get_HappyPath() {
        OptionTemplate template = OptionTemplate.builder().id(TemplateId.of(10L)).name("A").optionValues(List.of("X", "Y")).build();
        when(getTemplateUseCase.get(UserId.of(1L), TemplateId.of(10L))).thenReturn(template);

        TemplateResponse response = controller.get(10L);

        assertEquals(10L, response.getId());
    }

    @Test
    void get_NotOwnedOrMissing_ThrowsException() {
        // Regresión IDOR (mismo patrón que DecisionController): un usuario
        // no puede leer una plantilla ajena solo adivinando su id.
        when(getTemplateUseCase.get(UserId.of(1L), TemplateId.of(999L)))
                .thenThrow(new ResourceNotFoundException("Template not found"));

        assertThrows(ResourceNotFoundException.class, () -> controller.get(999L));
    }

    @Test
    void delete_HappyPath() {
        controller.delete(10L);

        verify(deleteTemplateUseCase).delete(UserId.of(1L), TemplateId.of(10L));
    }

    @Test
    void delete_NotOwnedOrMissing_ThrowsException() {
        doThrow(new ResourceNotFoundException("Template not found"))
                .when(deleteTemplateUseCase).delete(UserId.of(1L), TemplateId.of(999L));

        assertThrows(ResourceNotFoundException.class, () -> controller.delete(999L));
    }
}
