package com.gym.crm.workload.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Slf4j
@Component
public class ServiceJwtValidator {
    private static final String TYPE_CLAIM = "type";
    private static final String SERVICE_TYPE = "SERVICE";

    @Value("${jwt.secret}")
    private String jwtSecret;

    public boolean isValidServiceToken(String token) {
        try {
            Claims claims = extractAllClaims(token);

            return SERVICE_TYPE.equals(claims.get(TYPE_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn("Service token validation failed: {}", exception.getMessage());

            return false;
        }
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}