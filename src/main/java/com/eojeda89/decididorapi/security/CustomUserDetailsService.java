package com.eojeda89.decididorapi.security;

import com.eojeda89.decididorapi.application.port.out.UserRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new Exceptions.InvalidRequestException("invalid credentials"));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                // Aquí puedes agregar los roles del usuario si los tienes
                Collections.emptyList()
        );
    }
}