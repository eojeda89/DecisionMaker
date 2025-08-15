package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.adapter.in.web.dto.LoginRequest;
import com.eojeda89.decididorapi.application.service.AuthService;
import com.eojeda89.decididorapi.domain.model.User;
import com.eojeda89.decididorapi.domain.model.UserId;
import com.eojeda89.decididorapi.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LoginController controller;

    static class LoginRequestBuilder {
        private String usernameOrEmail = "user";
        private String password = "pass";

        public LoginRequestBuilder usernameOrEmail(String val) {
            this.usernameOrEmail = val;
            return this;
        }

        public LoginRequestBuilder password(String val) {
            this.password = val;
            return this;
        }

        public LoginRequest build() {
            LoginRequest req = new LoginRequest();
            req.setUsernameOrEmail(usernameOrEmail);
            req.setPassword(password);
            return req;
        }
    }

    static class UserBuilder {
        private String username = "user";
        private String email = "user@email.com";
        private UserId id = UserId.of(1L);

        public UserBuilder username(String val) {
            this.username = val;
            return this;
        }

        public UserBuilder email(String val) {
            this.email = val;
            return this;
        }

        public UserBuilder id(UserId val) {
            this.id = val;
            return this;
        }

        public User build() {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setId(id);
            return user;
        }
    }

    @Test
    void login_HappyPath() {
        LoginRequest request = new LoginRequestBuilder().build();
        User user = new UserBuilder().build();
        when(authService.authenticate("user", "pass")).thenReturn(user);
        when(jwtService.generateToken("user", 1L)).thenReturn("jwt-token");

        Map<String, Object> response = controller.login(request);

        assertEquals("jwt-token", response.get("token"));
        assertEquals("Bearer", response.get("tokenType"));
        assertEquals(3600000, response.get("expiresInMs"));
        verify(authService).authenticate("user", "pass");
        verify(jwtService).generateToken("user", 1L);
    }

    @Test
    void login_UserWithoutUsername_UsesEmail() {
        LoginRequest request = new LoginRequestBuilder().build();
        User user = new UserBuilder().username(null).email("mail@mail.com").build();
        when(authService.authenticate(anyString(), anyString())).thenReturn(user);
        when(jwtService.generateToken("mail@mail.com", 1L)).thenReturn("jwt-token");

        Map<String, Object> response = controller.login(request);

        assertEquals("jwt-token", response.get("token"));
        verify(jwtService).generateToken("mail@mail.com", 1L);
    }

    @Test
    void login_UserWithoutId_PassesNullToToken() {
        LoginRequest request = new LoginRequestBuilder().build();
        User user = new UserBuilder().id(null).build();
        when(authService.authenticate(anyString(), anyString())).thenReturn(user);
        when(jwtService.generateToken(anyString(), isNull())).thenReturn("jwt-token");

        Map<String, Object> response = controller.login(request);

        assertEquals("jwt-token", response.get("token"));
        verify(jwtService).generateToken(anyString(), isNull());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        LoginRequest request = new LoginRequestBuilder().build();
        when(authService.authenticate(anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid credentials"));

        assertThrows(RuntimeException.class, () -> controller.login(request));
    }

    @Test
    void login_NullRequest_ThrowsException() {
        assertThrows(NullPointerException.class, () -> controller.login(null));
    }
}