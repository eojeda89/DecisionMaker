package com.eojeda89.decididorapi.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    // Cada llamada usa un MockHttpServletRequest NUEVO (como en la vida
    // real, cada request HTTP es un objeto distinto) — OncePerRequestFilter
    // marca el request como "ya filtrado" con un atributo, así que reusar
    // el mismo objeto en un loop saltearía doFilterInternal() a partir de
    // la segunda llamada y el test no probaría nada.

    private final RateLimitFilter filter = new RateLimitFilter();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest request(String method, String uri, String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(ip);
        return request;
    }

    @Test
    void doFilter_UnlimitedPath_AlwaysPassesThrough() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 200; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request("GET", "/actuator/health", "10.0.0.1"), response, chain);
            assertEquals(200, response.getStatus());
        }
        verify(chain, times(200)).doFilter(any(), any());
    }

    @Test
    void doFilter_LoginPath_AllowsUpToLimitThenBlocks() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        // El límite de /auth/login es 10 por ventana (ver RateLimitFilter).
        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request("POST", "/auth/login", "10.0.0.1"), response, chain);
            assertNotEquals(429, response.getStatus(), "Request " + i + " no debería haber sido bloqueada");
        }
        verify(chain, times(10)).doFilter(any(), any());

        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        filter.doFilter(request("POST", "/auth/login", "10.0.0.1"), blockedResponse, chain);

        assertEquals(429, blockedResponse.getStatus());
        verify(chain, times(10)).doFilter(any(), any()); // no se llamó una 11ª vez
    }

    @Test
    void doFilter_DifferentIps_HaveIndependentLimits() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 10; i++) {
            filter.doFilter(request("POST", "/auth/login", "10.0.0.1"), new MockHttpServletResponse(), chain);
        }
        MockHttpServletResponse blockedIp1 = new MockHttpServletResponse();
        filter.doFilter(request("POST", "/auth/login", "10.0.0.1"), blockedIp1, chain);
        assertEquals(429, blockedIp1.getStatus());

        // Otra IP no está afectada por el límite de la primera.
        MockHttpServletResponse responseIp2 = new MockHttpServletResponse();
        filter.doFilter(request("POST", "/auth/login", "10.0.0.2"), responseIp2, chain);
        assertNotEquals(429, responseIp2.getStatus());
    }

    @Test
    void doFilter_AuthenticatedUser_IsKeyedByUsernameNotIp() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", null, List.of())
        );

        for (int i = 0; i < 60; i++) {
            filter.doFilter(request("GET", "/api/decisions", "10.0.0.1"), new MockHttpServletResponse(), chain);
        }
        MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(request("GET", "/api/decisions", "10.0.0.1"), blocked, chain);
        assertEquals(429, blocked.getStatus());

        // Mismo usuario, IP distinta: sigue bloqueado porque la clave es el
        // usuario autenticado, no la IP.
        MockHttpServletResponse stillBlocked = new MockHttpServletResponse();
        filter.doFilter(request("GET", "/api/decisions", "10.0.0.2"), stillBlocked, chain);
        assertEquals(429, stillBlocked.getStatus());
    }
}
