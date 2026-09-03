package com.eojeda89.decididorapi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * i18n (español/inglés/japonés): el locale se guarda en la sesión HTTP
 * (misma sesión que ya usa formLogin para la UI Thymeleaf) y se cambia
 * agregando "?lang=es|en|ja" a cualquier URL (ver fragments/navbar.html).
 * El default "es" se aplica explícitamente -- sin esto, SessionLocaleResolver
 * caería al Accept-Language del cliente, lo que cambiaría el idioma de las
 * respuestas de la API JSON según el navegador de quien llame, rompiendo la
 * compatibilidad con clientes existentes que asumen español.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.of("es"));
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
