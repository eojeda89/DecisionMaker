package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.out.UserRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticate_HappyPath_Username() {
        User user = new User();
        user.setPassword("encoded");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

        User result = authService.authenticate("user", "raw");

        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository).findByUsername("user");
        verify(passwordEncoder).matches("raw", "encoded");
    }

    @Test
    void authenticate_HappyPath_Email() {
        User user = new User();
        user.setPassword("encoded");
        when(userRepository.findByUsername("mail@mail.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

        User result = authService.authenticate("mail@mail.com", "raw");

        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository).findByUsername("mail@mail.com");
        verify(userRepository).findByEmail("mail@mail.com");
        verify(passwordEncoder).matches("raw", "encoded");
    }

    @Test
    void authenticate_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nouser")).thenReturn(Optional.empty());

        assertThrows(Exceptions.InvalidRequestException.class,
                () -> authService.authenticate("nouser", "raw"));
    }

    @Test
    void authenticate_PasswordMismatch_ThrowsException() {
        User user = new User();
        user.setPassword("encoded");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(Exceptions.InvalidRequestException.class,
                () -> authService.authenticate("user", "wrong"));
    }

    @Test
    void authenticate_NullUsernameOrEmail_ThrowsException() {
        assertThrows(Exceptions.InvalidRequestException.class, () -> authService.authenticate(null, "raw"));
    }

    @Test
    void authenticate_NullPassword_ThrowsException() {
        assertThrows(Exceptions.InvalidRequestException.class, () -> authService.authenticate("user", null));
    }
}