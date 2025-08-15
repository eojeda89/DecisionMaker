package com.eojeda89.decididorapi.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Test
    void generateTokenAndParse_HappyPath_ReturnsCorrectClaims() {
        JwtService service = new JwtService("super-secret-key-1234567890123456", 3600000);
        String token = service.generateToken("usuario", 42L);

        Claims claims = service.parse(token);

        assertEquals("usuario", claims.getSubject());
        assertEquals(42, claims.get("uid", Integer.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void generateToken_WithShortSecret_PadsKeyAndWorks() {
        JwtService service = new JwtService("short", 3600000);
        String token = service.generateToken("test", 1L);

        Claims claims = service.parse(token);

        assertEquals("test", claims.getSubject());
        assertEquals(1, claims.get("uid", Integer.class));
    }

    @Test
    void generateToken_WithZeroExpiration_ExpiresImmediately() throws InterruptedException {
        JwtService service = new JwtService("super-secret-key-1234567890123456", 0);
        String token = service.generateToken("exp", 99L);

        // Esperar para asegurar expiración
        Thread.sleep(5);

        assertThrows(ExpiredJwtException.class, () -> service.parse(token));
    }

    @Test
    void parse_InvalidToken_ThrowsException() {
        JwtService service = new JwtService("super-secret-key-1234567890123456", 3600000);

        assertThrows(Exception.class, () -> service.parse("token-invalido"));
    }
}