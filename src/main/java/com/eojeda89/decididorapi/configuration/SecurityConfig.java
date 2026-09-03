package com.eojeda89.decididorapi.configuration;

import com.eojeda89.decididorapi.security.RateLimitFilter;
import com.eojeda89.decididorapi.security.jwt.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthFilter jwtAuthFilter, RateLimitFilter rateLimitFilter,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers("/auth/register", "/auth/login", "/actuator/health", "/v3/api-docs/**",
                                "/swagger-ui/**", "/login", "/register", "/static/**", "/css/**", "/js/**",
                                "/api/decisions/shared/**", "/shared/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()
                        // El preflight CORS (OPTIONS) no manda el header
                        // Authorization: sin este permitAll, anyRequest()
                        // .authenticated() lo rechazaría antes de que el
                        // navegador pueda ver los headers CORS de la respuesta
                        // real, y cualquier front en otro origen se rompería.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login")
                                .loginProcessingUrl("/authenticate")
                                // Mejora de navegación (Fase 4): ir directo a
                                // decidir en vez de a la pantalla de bienvenida,
                                // que solo agregaba un clic extra al camino más
                                // común. "/" sigue existiendo como home (link de
                                // marca en la barra de navegación).
                                .defaultSuccessUrl("/form", true)
                                .permitAll()
                )
                .logout(LogoutConfigurer::permitAll)
                .addFilterAfter(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Después de JwtAuthFilter: para /api/decisions ya puede leer el
                // usuario autenticado del SecurityContext y limitar por usuario
                // en vez de por IP.
                .addFilterAfter(rateLimitFilter, JwtAuthFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS (para que cualquier frontend en otro origen pueda consumir la API
    // JSON, ver README): configurable vía app.cors.allowed-origins (lista
    // separada por comas), "*" por default. Solo se aplica a /api/**; las
    // páginas Thymeleaf no se consumen desde otro origen. allowCredentials
    // queda en false a propósito -- la API usa Bearer tokens (Authorization
    // header), no cookies, así que no hace falta y permite usar "*" como
    // origen sin violar la restricción de CORS que lo prohíbe junto a
    // credenciales.
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:*}") List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
