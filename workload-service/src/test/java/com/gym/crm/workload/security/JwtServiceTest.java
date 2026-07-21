package com.gym.crm.workload.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {
    private static final String JWT_SECRET = "VGhpc0lzQVN1cGVyU2VjcmV0S2V5Rm9ySldUVGVzdGluZzEyMzQ1Njc4OTA=";

    private JwtService jwtService;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "jwtSecret", JWT_SECRET);

        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
    }

    @Test
    void shouldExtractUsername() {
        String jwtToken = createToken("trainer");

        String actualUsername = jwtService.extractUsername(jwtToken);

        assertEquals("trainer", actualUsername);
    }

    @Test
    void shouldReturnTrueForValidToken() {
        String jwtToken = createToken("trainer");

        boolean actualResult = jwtService.isTokenValid(jwtToken);

        assertTrue(actualResult);
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject("trainer")
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 5_000))
                .signWith(signingKey)
                .compact();

        boolean actualResult = jwtService.isTokenValid(expiredToken);

        assertFalse(actualResult);
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid-token";

        boolean actualResult = jwtService.isTokenValid(invalidToken);

        assertFalse(actualResult);
    }

    @Test
    void shouldThrowExceptionWhenExtractUsernameFromInvalidToken() {
        String invalidToken = "invalid-token";

        Executable executable = () -> jwtService.extractUsername(invalidToken);

        assertThrows(Exception.class, executable);
    }

    @Test
    void shouldExtractExpiration() {
        Date expiration = new Date(System.currentTimeMillis() + 60_000);
        String jwtToken = Jwts.builder().subject("trainer").issuedAt(new Date()).expiration(expiration).signWith(signingKey).compact();

        Date actualExpiration = jwtService.extractExpiration(jwtToken);

        assertEquals(expiration.getTime() / 1000, actualExpiration.getTime() / 1000);
    }

    private String createToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();
    }
}