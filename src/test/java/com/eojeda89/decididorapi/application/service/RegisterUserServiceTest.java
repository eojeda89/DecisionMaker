package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.command.RegisterUserCommand;
import com.eojeda89.decididorapi.application.port.in.result.UserSummary;
import com.eojeda89.decididorapi.application.port.out.UserRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserService service;

    @Test
    void register_HappyPath() {
        RegisterUserCommand command = RegisterUserCommand.builder()
                .username("user")
                .email("mail@mail.com")
                .password("rawpass")
                .build();

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("mail@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("rawpass")).thenReturn("encodedpass");

        User saved = new User();
        saved.setId(new com.eojeda89.decididorapi.domain.model.UserId(1L));
        saved.setUsername("user");
        saved.setEmail("mail@mail.com");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserSummary result = service.register(command);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("user", result.getUsername());
        assertEquals("mail@mail.com", result.getEmail());
        verify(userRepository).existsByUsername("user");
        verify(userRepository).existsByEmail("mail@mail.com");
        verify(passwordEncoder).encode("rawpass");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameExists_ThrowsConflict() {
        RegisterUserCommand command = RegisterUserCommand.builder()
                .username("user")
                .email("mail@mail.com")
                .password("pass")
                .build();

        when(userRepository.existsByUsername("user")).thenReturn(true);

        assertThrows(Exceptions.ConflictException.class, () -> service.register(command));
        verify(userRepository).existsByUsername("user");
        verify(userRepository, never()).existsByEmail(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_EmailExists_ThrowsConflict() {
        RegisterUserCommand command = RegisterUserCommand.builder()
                .username("user")
                .email("mail@mail.com")
                .password("pass")
                .build();

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("mail@mail.com")).thenReturn(true);

        assertThrows(Exceptions.ConflictException.class, () -> service.register(command));
        verify(userRepository).existsByUsername("user");
        verify(userRepository).existsByEmail("mail@mail.com");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_NullCommand_ThrowsException() {
        assertThrows(NullPointerException.class, () -> service.register(null));
    }

    @Test
    void register_NullUsername_ThrowsException() {
        RegisterUserCommand command = RegisterUserCommand.builder()
                .username(null)
                .email("mail@mail.com")
                .password("pass")
                .build();

        assertThrows(NullPointerException.class, () -> service.register(command));
    }

    @Test
    void register_NullEmail_ThrowsException() {
        RegisterUserCommand command = RegisterUserCommand.builder()
                .username("user")
                .email(null)
                .password("pass")
                .build();

        assertThrows(NullPointerException.class, () -> service.register(command));
    }

    @Test
    void register_NullPassword_ThrowsException() {
        RegisterUserCommand command = RegisterUserCommand.builder()
                .username("user")
                .email("mail@mail.com")
                .password(null)
                .build();

        assertThrows(NullPointerException.class, () -> service.register(command));
    }
}