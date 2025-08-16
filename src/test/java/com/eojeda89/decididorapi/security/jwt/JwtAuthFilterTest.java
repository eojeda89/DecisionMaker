package com.eojeda89.decididorapi.security.jwt;

import com.eojeda89.decididorapi.application.port.out.UserRepository;
import com.eojeda89.decididorapi.domain.model.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    JwtService jwtService;
    @Mock
    UserRepository userRepository;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain filterChain;
    @Mock
    Claims claims;

    JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtService, userRepository);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_HappyPath_SetsAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");
        when(jwtService.parse("validtoken")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(mock(User.class)));

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user1", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_HeaderNull_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_HeaderWithoutBearer_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic abcdef");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
        when(jwtService.parse("invalidtoken")).thenThrow(new RuntimeException("Invalid token"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_UserNotFoundByUsernameOrEmail_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");
        when(jwtService.parse("validtoken")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user2");
        when(userRepository.findByUsername("user2")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user2")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_AuthenticationAlreadySet_DoesNotOverride() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("already", null, null)
        );
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");
        when(jwtService.parse("validtoken")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user3");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals("already", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }
}