package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.RegisterUserUseCase;
import com.eojeda89.decididorapi.application.port.in.command.RegisterUserCommand;
import com.eojeda89.decididorapi.application.port.in.result.UserSummary;
import com.eojeda89.decididorapi.application.port.out.UserRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserSummary register(RegisterUserCommand command) {
        if (userRepository.existsByUsername(command.getUsername())) {
            throw new Exceptions.ConflictException("username already exists");
        }
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new Exceptions.ConflictException("email already exists");
        }
        User user = new User();
        user.setUsername(command.getUsername());
        user.setEmail(command.getEmail());
        user.setPassword(passwordEncoder.encode(command.getPassword()));
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        User saved = userRepository.save(user);
        return new UserSummary(saved.getId() != null ? saved.getId().value() : null, saved.getUsername(), saved.getEmail());
    }
}
