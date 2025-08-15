package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.adapter.in.web.dto.LoginRequest;
import com.eojeda89.decididorapi.application.service.AuthService;
import com.eojeda89.decididorapi.domain.model.User;
import com.eojeda89.decididorapi.security.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.getUsernameOrEmail(), request.getPassword());
        String token = jwtService.generateToken(
                user.getUsername() != null ? user.getUsername() : user.getEmail(),
                user.getId() != null ? user.getId().value() : null
        );
        return Map.of(
                "token", token,
                "tokenType", "Bearer",
                "expiresInMs", 3600000
        );
    }
}
