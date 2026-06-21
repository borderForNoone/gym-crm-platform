package com.gym.crm.workload.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceJwtValidatorTest {
    private ServiceJwtValidator validator;
    private SecretKey key;
    private String secret;

    @BeforeEach
    void setUp() {
        validator = new ServiceJwtValidator();

        key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        secret = Base64.getEncoder().encodeToString(key.getEncoded());

        ReflectionTestUtils.setField(validator, "jwtSecret", secret);
    }

    private String createToken(String subject, String type) {
        return Jwts.builder()
                .subject(subject)
                .claims(Map.of("type", type))
                .issuedAt(new Date())
                .signWith(key)
                .compact();
    }

    @Test
    void shouldReturnTrue_whenServiceToken() {
        String token = createToken("service-a", "SERVICE");

        boolean result = validator.isValidServiceToken(token);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenNotServiceToken() {
        String token = createToken("service-a", "USER");

        boolean result = validator.isValidServiceToken(token);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalse_whenTokenInvalid() {
        String badToken = "invalid.token.here";

        boolean result = validator.isValidServiceToken(badToken);

        assertThat(result).isFalse();
    }

    @Test
    void shouldExtractSubject() {
        String token = createToken("my-service", "SERVICE");

        String subject = validator.extractSubject(token);

        assertThat(subject).isEqualTo("my-service");
    }
}