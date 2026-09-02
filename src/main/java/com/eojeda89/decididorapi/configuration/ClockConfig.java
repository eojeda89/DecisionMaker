package com.eojeda89.decididorapi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Reloj inyectable (UTC) en vez de llamar a Instant.now()/LocalDate.now()
 * directamente — permite fijar "hoy" en los tests (ej. DailyDecisionService,
 * Fase 3.5) sin depender del reloj real de la máquina.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
