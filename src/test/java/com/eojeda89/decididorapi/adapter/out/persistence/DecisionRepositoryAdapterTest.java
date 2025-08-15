package com.eojeda89.decididorapi.adapter.out.persistence;

import com.eojeda89.decididorapi.adapter.out.persistence.mapper.DecisionPersistenceMapper;
import com.eojeda89.decididorapi.adapter.out.persistence.repository.DecisionJpaRepository;
import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.util.Collections;
import java.util.List;

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
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        DecisionEntity entity1 = mock(DecisionEntity.class);
        DecisionEntity entity2 = mock(DecisionEntity.class);
        List<DecisionEntity> entities = List.of(entity1, entity2);

        Decision decision1 = mock(Decision.class);
        Decision decision2 = mock(Decision.class);

        when(jpaRepository.findByUser(any(UserEntity.class))).thenReturn(entities);
        when(mapper.toDomain(entity1)).thenReturn(decision1);
        when(mapper.toDomain(entity2)).thenReturn(decision2);

        List<Decision> result = adapter.findByUser(userId);

        assertEquals(2, result.size());
        assertTrue(result.contains(decision1));
        assertTrue(result.contains(decision2));
        verify(jpaRepository).findByUser(argThat(u -> u.getId().equals(1L)));
        verify(mapper).toDomain(entity1);
        verify(mapper).toDomain(entity2);
    }

    @Test
    void findByUser_NoDecisions_ReturnsEmptyList() {
        UserId userId = UserId.of(2L);
        when(jpaRepository.findByUser(any(UserEntity.class))).thenReturn(Collections.emptyList());

        List<Decision> result = adapter.findByUser(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUser_NullUserId_ThrowsException() {
        assertThrows(NullPointerException.class, () -> adapter.findByUser(null));
    }
}