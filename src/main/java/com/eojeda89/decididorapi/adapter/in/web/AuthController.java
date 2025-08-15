package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.application.port.in.RegisterUserUseCase;
import com.eojeda89.decididorapi.application.port.in.command.RegisterUserCommand;
import com.eojeda89.decididorapi.application.port.in.result.UserSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;

    @PostMapping("/register")
    public ResponseEntity<UserSummary> register(@Valid @RequestBody RegisterUserCommand command) {
        UserSummary user = registerUserUseCase.register(command);
        return ResponseEntity.ok(user);
    }
}
