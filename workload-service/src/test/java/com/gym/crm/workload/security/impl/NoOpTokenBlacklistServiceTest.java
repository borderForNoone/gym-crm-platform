package com.gym.crm.workload.security.impl;

import com.gym.crm.workload.security.imp.NoOpTokenBlacklistService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

class NoOpTokenBlacklistServiceTest {
    private final NoOpTokenBlacklistService service = new NoOpTokenBlacklistService();

    @Test
    void shouldDoNothingWhenBlacklistToken() {
        String token = "jwt-token";

        Throwable result = catchThrowable(() -> service.blacklist(token));

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnFalseForAnyToken() {
        String token = "jwt-token";

        boolean result = service.isBlacklisted(token);

        assertThat(result).isFalse();
    }

    @Test
    void shouldAlwaysReturnFalseAfterBlacklist() {
        String token = "jwt-token";
        service.blacklist(token);

        boolean result = service.isBlacklisted(token);

        assertThat(result).isFalse();
    }

    @Test
    void shouldHandleNullToken() {
        Throwable blacklistResult = catchThrowable(() -> service.blacklist(null));

        boolean isBlacklisted = service.isBlacklisted(null);

        assertThat(blacklistResult).isNull();
        assertThat(isBlacklisted).isFalse();
    }
}