package com.eojeda89.decididorapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting simple en memoria, por ventana fija de 1 minuto, para
 * /auth/login (clave: IP del cliente, todavía no hay usuario autenticado)
 * y /api/decisions (clave: usuario autenticado si lo hay, si no la IP).
 * <p>
 * No pretende ser exacto ni distribuido — alcanza para frenar fuerza
 * bruta/abuso básico en un proyecto de este tamaño corriendo en una sola
 * instancia. Limitaciones conocidas y aceptadas a este tamaño de proyecto:
 * si algún día corre en más de una instancia, cada una cuenta por separado
 * (habría que mover el contador a algo compartido, ej. Redis); y el mapa de
 * ventanas por clave no se limpia, así que crece lentamente con el tráfico
 * (irrelevante en la escala de este proyecto).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000;
    private static final int LOGIN_LIMIT_PER_WINDOW = 10;
    private static final int DECISIONS_LIMIT_PER_WINDOW = 60;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Integer limit = limitFor(request.getRequestURI());
        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRequestURI() + "|" + resolveKey(request);
        if (!allow(key, limit)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded, try again later\",\"status\":429}"
            );
            return;
        }
        filterChain.doFilter(request, response);
    }

    private Integer limitFor(String path) {
        if ("/auth/login".equals(path)) return LOGIN_LIMIT_PER_WINDOW;
        if (path.startsWith("/api/decisions")) return DECISIONS_LIMIT_PER_WINDOW;
        return null;
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return "user:" + auth.getName();
        }
        return "ip:" + clientIp(request);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean allow(String key, int limit) {
        long now = System.currentTimeMillis();
        Window window = windows.compute(key, (k, existing) ->
                (existing == null || now - existing.windowStart >= WINDOW_MILLIS) ? new Window(now) : existing
        );
        return window.count.incrementAndGet() <= limit;
    }

    private static final class Window {
        final long windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
