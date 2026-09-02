package com.eojeda89.decididorapi.application.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ShareCodeGeneratorTest {

    @Test
    void generate_ReturnsEightAlphanumericChars() {
        String code = ShareCodeGenerator.generate();

        assertEquals(8, code.length());
        assertTrue(code.matches("[A-Za-z0-9]+"));
    }

    @Test
    void generate_ManyCalls_AreEffectivelyUnique() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            codes.add(ShareCodeGenerator.generate());
        }

        assertEquals(10_000, codes.size(), "No debería haber colisiones en 10.000 generaciones");
    }
}
