package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.application.port.in.RegisterUserUseCase;
import com.eojeda89.decididorapi.application.port.in.command.RegisterUserCommand;
import com.eojeda89.decididorapi.application.port.in.result.UserSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private RegisterUserUseCase registerUserUseCase;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        registerUserUseCase = mock(RegisterUserUseCase.class);
        authController = new AuthController(registerUserUseCase);
    }

    @Test
    void register_ReturnsUserSummary() {
        RegisterUserCommand command = new RegisterUserCommand(/* inicializa los campos necesarios */);
        UserSummary expectedUser = new UserSummary(/* inicializa los campos necesarios */);

        when(registerUserUseCase.register(ArgumentMatchers.any(RegisterUserCommand.class)))
                .thenReturn(expectedUser);

        ResponseEntity<UserSummary> response = authController.register(command);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedUser, response.getBody());
        verify(registerUserUseCase, times(1)).register(command);
    }
}