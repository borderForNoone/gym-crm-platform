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
    private static final String SERVICE_TYPE = "SERVICE";
    private static final String USER_TYPE = "USER";
    private static final String SERVICE_SUBJECT = "service-a";
    private static final String OTHER_SERVICE_SUBJECT = "my-service";
    private static final String INVALID_TOKEN = "invalid.token.here";

    private SecretKey key;
    private String secret;
    private ServiceJwtValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ServiceJwtValidator();
        key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        secret = Base64.getEncoder().encodeToString(key.getEncoded());

        ReflectionTestUtils.setField(validator, "jwtSecret", secret);
    }

    @Test
    void shouldReturnTrue_whenServiceToken() {
        String token = createToken(SERVICE_SUBJECT, SERVICE_TYPE);
        boolean expected = true;

        boolean actual = validator.isValidServiceToken(token);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnFalse_whenNotServiceToken() {
        String token = createToken(SERVICE_SUBJECT, USER_TYPE);
        boolean expected = false;

        boolean actual = validator.isValidServiceToken(token);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnFalse_whenTokenInvalid() {
        boolean expected = false;

        boolean actual = validator.isValidServiceToken(INVALID_TOKEN);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldExtractSubject() {
        String token = createToken(OTHER_SERVICE_SUBJECT, SERVICE_TYPE);
        String expected = OTHER_SERVICE_SUBJECT;

        String actual = validator.extractSubject(token);

        assertThat(actual).isEqualTo(expected);
    }

    private String createToken(String subject, String type) {
        return Jwts.builder()
                .subject(subject)
                .claims(Map.of("type", type))
                .issuedAt(new Date())
                .signWith(key)
                .compact();
    }
}