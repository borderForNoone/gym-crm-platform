package com.gym.crm.core.client.workload;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ServiceTokenProvider {
    private static final String SERVICE_SUBJECT = "gym-core-service";
    private static final String TYPE_CLAIM = "type";
    private static final String SERVICE_TYPE = "SERVICE";
    private static final long REFRESH_MARGIN_SECONDS = 30;

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.service-expiration:300000}")
    private long serviceTokenExpirationMs;

    private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();

    public String getToken() {
        CachedToken current = cachedToken.get();
        if (current != null && !current.isCloseToExpiry()) {
            return current.value();
        }

        CachedToken generated = generateToken();
        cachedToken.set(generated);

        return generated.value();
    }

    private CachedToken generateToken() {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(serviceTokenExpirationMs);

        String token = Jwts.builder()
                .subject(SERVICE_SUBJECT)
                .claims(Map.of(TYPE_CLAIM, SERVICE_TYPE))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSecretKey())
                .compact();

        return new CachedToken(token, expiresAt);
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    private record CachedToken(String value, Instant expiresAt) {
        boolean isCloseToExpiry() {
            return Instant.now().plusSeconds(REFRESH_MARGIN_SECONDS).isAfter(expiresAt);
        }
    }
}