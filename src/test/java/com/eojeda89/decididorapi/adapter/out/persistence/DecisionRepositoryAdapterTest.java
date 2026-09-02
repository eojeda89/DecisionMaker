package com.eojeda89.decididorapi.adapter.out.persistence;

import com.eojeda89.decididorapi.adapter.out.persistence.mapper.DecisionPersistenceMapper;
import com.eojeda89.decididorapi.adapter.out.persistence.repository.DecisionJpaRepository;
import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DecisionRepositoryAdapterTest {

    @Mock
    private DecisionJpaRepository jpaRepository;
    @Mock
    private DecisionPersistenceMapper mapper;

    @InjectMocks
    private DecisionRepositoryAdapter adapter;

    @Test
    void save_HappyPath() {
        Decision decision = mock(Decision.class);
        DecisionEntity entity = mock(DecisionEntity.class);
        DecisionEntity savedEntity = mock(DecisionEntity.class);
        Decision mappedDecision = mock(Decision.class);

        when(mapper.toEntity(decision)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(mappedDecision);

        Decision result = adapter.save(decision);

        assertNotNull(result);
        assertEquals(mappedDecision, result);
        verify(mapper).toEntity(decision);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    void save_NullDecision_ThrowsException() {
        assertThrows(NullPointerException.class, () -> adapter.save(null));
    }

    @Test
    void findByUser_HappyPath() {
        UserId userId = UserId.of(1L);
        Pageable pageable = PageRequest.of(0, 20);

        DecisionEntity entity1 = mock(DecisionEntity.class);
        DecisionEntity entity2 = mock(DecisionEntity.class);
        when(entity1.getId()).thenReturn(10L);
        when(entity2.getId()).thenReturn(20L);

        Decision decision1 = mock(Decision.class);
        Decision decision2 = mock(Decision.class);

        // findIdsByUser define el orden de la página; findByIdInWithOptions
        // puede devolver las entidades en cualquier orden (JOIN FETCH no lo
        // garantiza) — acá las devuelve al revés a propósito para probar
        // que el adapter reordena según los ids.
        Page<Long> idsPage = new PageImpl<>(List.of(10L, 20L), pageable, 2);
        when(jpaRepository.findIdsByUser(any(UserEntity.class), eq(pageable))).thenReturn(idsPage);
        when(jpaRepository.findByIdInWithOptions(List.of(10L, 20L))).thenReturn(List.of(entity2, entity1));
        when(mapper.toDomain(entity1)).thenReturn(decision1);
        when(mapper.toDomain(entity2)).thenReturn(decision2);

        Page<Decision> result = adapter.findByUser(userId, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(decision1, decision2), result.getContent());
        verify(jpaRepository).findIdsByUser(argThat(u -> u.getId().equals(1L)), eq(pageable));
    }

    @Test
    void findByUser_NoDecisions_ReturnsEmptyPage() {
        UserId userId = UserId.of(2L);
        Pageable pageable = PageRequest.of(0, 20);
        when(jpaRepository.findIdsByUser(any(UserEntity.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        Page<Decision> result = adapter.findByUser(userId, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(jpaRepository, never()).findByIdInWithOptions(any());
    }

    @Test
    void findByUser_NullUserId_ThrowsException() {
        assertThrows(NullPointerException.class, () -> adapter.findByUser(null, PageRequest.of(0, 20)));
    }

    @Test
    void findByShareCode_HappyPath() {
        DecisionEntity entity = mock(DecisionEntity.class);
        Decision decision = mock(Decision.class);
        when(jpaRepository.findByShareCode("ABCDEFGH")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(decision);

        Optional<Decision> result = adapter.findByShareCode("ABCDEFGH");

        assertTrue(result.isPresent());
        assertEquals(decision, result.get());
    }

    @Test
    void findByShareCode_NotFound_ReturnsEmpty() {
        when(jpaRepository.findByShareCode("NOEXISTE")).thenReturn(Optional.empty());

        Optional<Decision> result = adapter.findByShareCode("NOEXISTE");

        assertTrue(result.isEmpty());
    }
}
