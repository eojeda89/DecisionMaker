package com.eojeda89.decididorapi.application.service;

import java.security.SecureRandom;

/**
 * Genera el código corto de "sala compartida" (Fase 3.3) que se asigna a
 * cada decisión al crearla, usado en GET /api/decisions/shared/{code} para
 * que cualquiera con el link vea el resultado sin login. 8 caracteres
 * alfanuméricos (62^8 ≈ 218 billones de combinaciones): no hace falta
 * reintentar en caso de colisión, es lo bastante improbable para el tamaño
 * de este proyecto.
 */
final class ShareCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private ShareCodeGenerator() {
    }

    static String generate() {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
