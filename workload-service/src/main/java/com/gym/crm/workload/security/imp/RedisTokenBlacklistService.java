package com.gym.crm.workload.security.imp;

import com.gym.crm.workload.security.JwtService;
import com.gym.crm.workload.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Service
@Profile("!automation")
@RequiredArgsConstructor
public class RedisTokenBlacklistService implements TokenBlacklistService {
    private final StringRedisTemplate template;
    private final JwtService jwtService;

    @Override
    public void blacklist(String token) {
        Date expirationDate = jwtService.extractExpiration(token);
        long remainingTime = expirationDate.getTime() - System.currentTimeMillis();

        if (remainingTime <= 0) {
            return;
        }

        String tokenHash = hashToken(token);
        template.opsForValue().set("blacklist:" + tokenHash, "true", remainingTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return template.hasKey("blacklist:" + hashToken(token));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}