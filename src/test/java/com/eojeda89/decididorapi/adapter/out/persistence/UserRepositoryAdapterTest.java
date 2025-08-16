package com.eojeda89.decididorapi.adapter.out.persistence;

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

    private static final String USERNAME = "user";
    private static final String NO_USERNAME = "noUser";
    private static final String EMAIL = "mail@mail.com";
    private static final String NO_EMAIL = "no@mail.com";

    @Mock
    private UserJpaRepository jpa;
    @Mock
    private UserPersistenceMapper mapper;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Test
    void existsByUsername_HappyPath_True() {
        when(jpa.existsByUsername(USERNAME)).thenReturn(true);
        assertTrue(adapter.existsByUsername(USERNAME));
        verify(jpa).existsByUsername(USERNAME);
    }

    @Test
    void existsByUsername_HappyPath_False() {
        when(jpa.existsByUsername(NO_USERNAME)).thenReturn(false);
        assertFalse(adapter.existsByUsername(NO_USERNAME));
        verify(jpa).existsByUsername(NO_USERNAME);
    }

    @Test
    void existsByUsername_NullUsername() {
        when(jpa.existsByUsername(null)).thenReturn(false);
        assertFalse(adapter.existsByUsername(null));
        verify(jpa).existsByUsername(null);
    }

    @Test
    void existsByEmail_HappyPath_True() {
        when(jpa.existsByEmail(EMAIL)).thenReturn(true);
        assertTrue(adapter.existsByEmail(EMAIL));
        verify(jpa).existsByEmail(EMAIL);
    }

    @Test
    void existsByEmail_HappyPath_False() {
        when(jpa.existsByEmail(NO_EMAIL)).thenReturn(false);
        assertFalse(adapter.existsByEmail(NO_EMAIL));
        verify(jpa).existsByEmail(NO_EMAIL);
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
        when(jpa.findByUsername(USERNAME)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(user);

        Optional<User> result = adapter.findByUsername(USERNAME);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpa).findByUsername(USERNAME);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findByUsername_NotFound() {
        when(jpa.findByUsername(NO_USERNAME)).thenReturn(Optional.empty());
        Optional<User> result = adapter.findByUsername(NO_USERNAME);
        assertTrue(result.isEmpty());
        verify(jpa).findByUsername(NO_USERNAME);
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
        when(jpa.findByEmail(EMAIL)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(user);

        Optional<User> result = adapter.findByEmail(EMAIL);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpa).findByEmail(EMAIL);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findByEmail_NotFound() {
        when(jpa.findByEmail(NO_EMAIL)).thenReturn(Optional.empty());
        Optional<User> result = adapter.findByEmail(NO_EMAIL);
        assertTrue(result.isEmpty());
        verify(jpa).findByEmail(NO_EMAIL);
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