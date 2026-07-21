package com.gym.crm.workload.security;

import com.gym.crm.workload.security.imp.RedisTokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
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

    @InjectMocks
    private RedisTokenBlacklistService service;

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

            assertThatThrownBy(() -> service.isBlacklisted(TOKEN))
                    .isInstanceOf(IllegalStateException.class)
                    .hasCause(cause)
                    .hasMessageContaining("SHA-256 is missing");
            verify(redisTemplate, never()).hasKey(anyString());
        }
    }
}
