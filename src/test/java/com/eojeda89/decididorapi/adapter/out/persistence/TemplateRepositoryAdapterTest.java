package com.eojeda89.decididorapi.adapter.out.persistence;

import com.eojeda89.decididorapi.adapter.out.persistence.mapper.OptionTemplatePersistenceMapper;
import com.eojeda89.decididorapi.adapter.out.persistence.repository.OptionTemplateJpaRepository;
import com.eojeda89.decididorapi.domain.model.OptionTemplate;
import com.eojeda89.decididorapi.domain.model.TemplateId;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateRepositoryAdapterTest {

    @Mock
    private OptionTemplateJpaRepository jpaRepository;
    @Mock
    private OptionTemplatePersistenceMapper mapper;

    @InjectMocks
    private TemplateRepositoryAdapter adapter;

    @Test
    void save_HappyPath() {
        OptionTemplate template = mock(OptionTemplate.class);
        OptionTemplateEntity entity = mock(OptionTemplateEntity.class);
        OptionTemplateEntity savedEntity = mock(OptionTemplateEntity.class);
        OptionTemplate mapped = mock(OptionTemplate.class);
        when(mapper.toEntity(template)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(mapped);

        OptionTemplate result = adapter.save(template);

        assertEquals(mapped, result);
    }

    @Test
    void save_NullTemplate_ThrowsException() {
        assertThrows(NullPointerException.class, () -> adapter.save(null));
    }

    @Test
    void findByUser_HappyPath() {
        OptionTemplateEntity entity = mock(OptionTemplateEntity.class);
        OptionTemplate template = mock(OptionTemplate.class);
        when(jpaRepository.findByUser(any(UserEntity.class))).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(template);

        List<OptionTemplate> result = adapter.findByUser(UserId.of(1L));

        assertEquals(List.of(template), result);
        verify(jpaRepository).findByUser(argThat(u -> u.getId().equals(1L)));
    }

    @Test
    void findById_Found() {
        OptionTemplateEntity entity = mock(OptionTemplateEntity.class);
        OptionTemplate template = mock(OptionTemplate.class);
        when(jpaRepository.findByIdWithOptions(10L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(template);

        Optional<OptionTemplate> result = adapter.findById(TemplateId.of(10L));

        assertTrue(result.isPresent());
        assertEquals(template, result.get());
    }

    @Test
    void findById_NotFound_ReturnsEmpty() {
        when(jpaRepository.findByIdWithOptions(999L)).thenReturn(Optional.empty());

        assertTrue(adapter.findById(TemplateId.of(999L)).isEmpty());
    }

    @Test
    void deleteById_DelegatesToJpaRepository() {
        adapter.deleteById(TemplateId.of(10L));

        verify(jpaRepository).deleteById(10L);
    }
}
