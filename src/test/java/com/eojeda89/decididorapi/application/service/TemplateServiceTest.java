package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.command.CreateTemplateCommand;
import com.eojeda89.decididorapi.application.port.out.TemplateRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.OptionTemplate;
import com.eojeda89.decididorapi.domain.model.TemplateId;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    private TemplateService service() {
        return new TemplateService(templateRepository);
    }

    @Test
    void create_HappyPath() {
        CreateTemplateCommand command = CreateTemplateCommand.builder()
                .userId(UserId.of(1L)).name("Restaurantes").optionValues(List.of("A", "B")).build();
        OptionTemplate saved = OptionTemplate.builder().id(TemplateId.of(10L)).userId(UserId.of(1L))
                .name("Restaurantes").optionValues(List.of("A", "B")).build();
        when(templateRepository.save(any(OptionTemplate.class))).thenReturn(saved);

        OptionTemplate result = service().create(command);

        assertEquals(TemplateId.of(10L), result.getId());
    }

    @Test
    void create_FewerThanTwoOptions_ThrowsException() {
        CreateTemplateCommand command = CreateTemplateCommand.builder()
                .userId(UserId.of(1L)).name("X").optionValues(List.of("SoloUno")).build();

        assertThrows(Exceptions.InvalidRequestException.class, () -> service().create(command));
    }

    @Test
    void create_NullCommand_ThrowsException() {
        assertThrows(NullPointerException.class, () -> service().create(null));
    }

    @Test
    void listByUser_HappyPath() {
        when(templateRepository.findByUser(UserId.of(1L))).thenReturn(List.of(mock(OptionTemplate.class)));

        List<OptionTemplate> result = service().listByUser(UserId.of(1L));

        assertEquals(1, result.size());
    }

    @Test
    void get_OwnedByRequester_ReturnsTemplate() {
        OptionTemplate template = OptionTemplate.builder().id(TemplateId.of(10L)).userId(UserId.of(1L)).build();
        when(templateRepository.findById(TemplateId.of(10L))).thenReturn(Optional.of(template));

        OptionTemplate result = service().get(UserId.of(1L), TemplateId.of(10L));

        assertEquals(template, result);
    }

    @Test
    void get_OwnedBySomeoneElse_ThrowsResourceNotFound() {
        // No debería poder distinguir "no existe" de "es de otro usuario":
        // ambos casos tiran la misma excepción, para no filtrar existencia.
        OptionTemplate template = OptionTemplate.builder().id(TemplateId.of(10L)).userId(UserId.of(2L)).build();
        when(templateRepository.findById(TemplateId.of(10L))).thenReturn(Optional.of(template));

        assertThrows(Exceptions.ResourceNotFoundException.class,
                () -> service().get(UserId.of(1L), TemplateId.of(10L)));
    }

    @Test
    void get_DoesNotExist_ThrowsResourceNotFound() {
        when(templateRepository.findById(TemplateId.of(999L))).thenReturn(Optional.empty());

        assertThrows(Exceptions.ResourceNotFoundException.class,
                () -> service().get(UserId.of(1L), TemplateId.of(999L)));
    }

    @Test
    void delete_OwnedByRequester_Deletes() {
        OptionTemplate template = OptionTemplate.builder().id(TemplateId.of(10L)).userId(UserId.of(1L)).build();
        when(templateRepository.findById(TemplateId.of(10L))).thenReturn(Optional.of(template));

        service().delete(UserId.of(1L), TemplateId.of(10L));

        verify(templateRepository).deleteById(TemplateId.of(10L));
    }

    @Test
    void delete_OwnedBySomeoneElse_ThrowsResourceNotFound_AndDoesNotDelete() {
        OptionTemplate template = OptionTemplate.builder().id(TemplateId.of(10L)).userId(UserId.of(2L)).build();
        when(templateRepository.findById(TemplateId.of(10L))).thenReturn(Optional.of(template));

        assertThrows(Exceptions.ResourceNotFoundException.class,
                () -> service().delete(UserId.of(1L), TemplateId.of(10L)));
        verify(templateRepository, never()).deleteById(any());
    }
}
