package com.gym.crm.core.security.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTokenBlacklistServiceTest {
    private InMemoryTokenBlacklistService service;

    @BeforeEach
    void setUp() {
        service = new InMemoryTokenBlacklistService();
    }

    @Test
    void shouldReturnFalseWhenTokenIsNotBlacklisted() {
        boolean result = service.isBlacklisted("valid-token");

        assertThat(result).isFalse();
    }

    @Test
    void shouldBlacklistToken() {
        String token = "jwt-token";

        service.blacklist(token);

        assertThat(service.isBlacklisted(token)).isTrue();
    }

    @Test
    void shouldKeepTokensIndependent() {
        service.blacklist("blacklisted-token");

        assertThat(service.isBlacklisted("blacklisted-token")).isTrue();
        assertThat(service.isBlacklisted("another-token")).isFalse();
    }

    @Test
    void shouldHandleSameTokenBlacklistedMultipleTimes() {
        String token = "jwt-token";

        service.blacklist(token);
        service.blacklist(token);

        assertThat(service.isBlacklisted(token)).isTrue();
    }

    @Test
    void shouldBlacklistEmptyToken() {
        String token = "";

        service.blacklist(token);

        assertThat(service.isBlacklisted(token)).isTrue();
    }
}