package com.eojeda89.decididorapi.adapter.out.persistence;

import com.eojeda89.decididorapi.adapter.out.persistence.UserEntity;
import com.eojeda89.decididorapi.adapter.out.persistence.UserRepositoryAdapter;
import com.eojeda89.decididorapi.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.eojeda89.decididorapi.adapter.out.persistence.repository.UserJpaRepository;
import com.eojeda89.decididorapi.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private UserJpaRepository jpa;
    @Mock
    private UserPersistenceMapper mapper;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Test
    void existsByUsername_HappyPath_True() {
        when(jpa.existsByUsername("user")).thenReturn(true);
        assertTrue(adapter.existsByUsername("user"));
        verify(jpa).existsByUsername("user");
    }

    @Test
    void existsByUsername_HappyPath_False() {
        when(jpa.existsByUsername("nouser")).thenReturn(false);
        assertFalse(adapter.existsByUsername("nouser"));
        verify(jpa).existsByUsername("nouser");
    }

    @Test
    void existsByUsername_NullUsername() {
        when(jpa.existsByUsername(null)).thenReturn(false);
        assertFalse(adapter.existsByUsername(null));
        verify(jpa).existsByUsername(null);
    }

    @Test
    void existsByEmail_HappyPath_True() {
        when(jpa.existsByEmail("mail@mail.com")).thenReturn(true);
        assertTrue(adapter.existsByEmail("mail@mail.com"));
        verify(jpa).existsByEmail("mail@mail.com");
    }

    @Test
    void existsByEmail_HappyPath_False() {
        when(jpa.existsByEmail("no@mail.com")).thenReturn(false);
        assertFalse(adapter.existsByEmail("no@mail.com"));
        verify(jpa).existsByEmail("no@mail.com");
    }

    @Test
    void existsByEmail_NullEmail() {
        when(jpa.existsByEmail(null)).thenReturn(false);
        assertFalse(adapter.existsByEmail(null));
        verify(jpa).existsByEmail(null);
    }

    @Test
    void findByUsername_HappyPath() {
        UserEntity entity = mock(UserEntity.class);
        User user = mock(User.class);
        when(jpa.findByUsername("user")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(user);

        Optional<User> result = adapter.findByUsername("user");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpa).findByUsername("user");
        verify(mapper).toDomain(entity);
    }

    @Test
    void findByUsername_NotFound() {
        when(jpa.findByUsername("nouser")).thenReturn(Optional.empty());
        Optional<User> result = adapter.findByUsername("nouser");
        assertTrue(result.isEmpty());
        verify(jpa).findByUsername("nouser");
        verifyNoInteractions(mapper);
    }

    @Test
    void findByUsername_NullUsername() {
        when(jpa.findByUsername(null)).thenReturn(Optional.empty());
        Optional<User> result = adapter.findByUsername(null);
        assertTrue(result.isEmpty());
        verify(jpa).findByUsername(null);
        verifyNoInteractions(mapper);
    }

    @Test
    void findByEmail_HappyPath() {
        UserEntity entity = mock(UserEntity.class);
        User user = mock(User.class);
        when(jpa.findByEmail("mail@mail.com")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(user);

        Optional<User> result = adapter.findByEmail("mail@mail.com");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpa).findByEmail("mail@mail.com");
        verify(mapper).toDomain(entity);
    }

    @Test
    void findByEmail_NotFound() {
        when(jpa.findByEmail("no@mail.com")).thenReturn(Optional.empty());
        Optional<User> result = adapter.findByEmail("no@mail.com");
        assertTrue(result.isEmpty());
        verify(jpa).findByEmail("no@mail.com");
        verifyNoInteractions(mapper);
    }

    @Test
    void findByEmail_NullEmail() {
        when(jpa.findByEmail(null)).thenReturn(Optional.empty());
        Optional<User> result = adapter.findByEmail(null);
        assertTrue(result.isEmpty());
        verify(jpa).findByEmail(null);
        verifyNoInteractions(mapper);
    }

    @Test
    void save_HappyPath() {
        User user = mock(User.class);
        UserEntity entity = mock(UserEntity.class);
        UserEntity savedEntity = mock(UserEntity.class);
        User mappedUser = mock(User.class);

        when(mapper.toEntity(user)).thenReturn(entity);
        when(jpa.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(mappedUser);

        User result = adapter.save(user);

        assertNotNull(result);
        assertEquals(mappedUser, result);
        verify(mapper).toEntity(user);
        verify(jpa).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    void save_NullUser_ThrowsException() {
        assertThrows(NullPointerException.class, () -> adapter.save(null));
    }
}