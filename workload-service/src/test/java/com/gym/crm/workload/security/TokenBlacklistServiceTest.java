package com.gym.crm.workload.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {
    private static final String TOKEN = "jwt-token";
    private static final String HASHED_TOKEN = "637dca1ed85901f74d2634ec978c3e441598b7cc2f86a2b9a004662222009808";
    private static final String BLACKLIST_KEY = "blacklist:" + HASHED_TOKEN;

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private JwtService jwtService;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @InjectMocks
    private TokenBlacklistService service;

    @Test
    void isBlacklisted_shouldReturnTrue_WhenKeyExistsInRedis() {
        when(redisTemplate.hasKey(BLACKLIST_KEY)).thenReturn(true);

        boolean actual = service.isBlacklisted(TOKEN);

        assertThat(actual).isTrue();
        verify(redisTemplate).hasKey(BLACKLIST_KEY);
    }

    @Test
    void isBlacklisted_shouldReturnFalseForUnknownToken() {
        when(redisTemplate.hasKey(BLACKLIST_KEY)).thenReturn(false);

        boolean actual = service.isBlacklisted(TOKEN);

        assertThat(actual).isFalse();
        verify(redisTemplate).hasKey(BLACKLIST_KEY);
    }

    @Test
    void isBlacklisted_whenHashingAlgorithmIsNotAvailable_shouldThrowIllegalStateException() {
        NoSuchAlgorithmException cause = new NoSuchAlgorithmException("SHA-256 is missing");

        try (MockedStatic<MessageDigest> messageDigest = Mockito.mockStatic(MessageDigest.class)) {
            messageDigest.when(() -> MessageDigest.getInstance("SHA-256")).thenThrow(cause);

            assertThatThrownBy(() -> service.isBlacklisted(TOKEN)).isInstanceOf(IllegalStateException.class).hasCause(cause);
            verify(redisTemplate, never()).hasKey(anyString());
        }
    }


    @Test
    void blacklist_shouldSaveTokenWhenExpirationIsInFuture() {
        Date expirationDate = new Date(System.currentTimeMillis() + 60_000);

        when(jwtService.extractExpiration(TOKEN)).thenReturn(expirationDate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.blacklist(TOKEN);

        verify(valueOperations).set(eq(BLACKLIST_KEY), eq("true"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }


    @Test
    void blacklist_shouldNotSaveTokenWhenExpirationIsExpired() {
        Date expirationDate = new Date(System.currentTimeMillis() - 1000);

        when(jwtService.extractExpiration(TOKEN)).thenReturn(expirationDate);

        service.blacklist(TOKEN);
        verify(redisTemplate, never()).opsForValue();
    }


    @Test
    void blacklist_whenHashingAlgorithmIsNotAvailable_shouldThrowIllegalStateException() {
        Date expirationDate = new Date(System.currentTimeMillis() + 60_000);

        when(jwtService.extractExpiration(TOKEN)).thenReturn(expirationDate);

        try (MockedStatic<MessageDigest> messageDigest = Mockito.mockStatic(MessageDigest.class)) {
            messageDigest.when(() -> MessageDigest.getInstance("SHA-256")).thenThrow(new NoSuchAlgorithmException());

            assertThatThrownBy(() -> service.blacklist(TOKEN)).isInstanceOf(IllegalStateException.class);
        }
    }
}
