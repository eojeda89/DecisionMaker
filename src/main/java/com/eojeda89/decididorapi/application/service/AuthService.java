package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.out.UserRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User authenticate(String usernameOrEmail, String rawPassword) {
        var user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new Exceptions.InvalidRequestException("invalid credentials"));
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new Exceptions.InvalidRequestException("invalid credentials");
        }
        return user;
    }
}
