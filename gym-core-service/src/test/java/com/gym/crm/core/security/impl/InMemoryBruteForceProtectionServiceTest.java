package com.gym.crm.core.security.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.LockedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryBruteForceProtectionServiceTest {
    private InMemoryBruteForceProtectionService service;

    @BeforeEach
    void setUp() {
        service = new InMemoryBruteForceProtectionService();
    }

    @Test
    void shouldNotThrowExceptionForUnlockedUser() {
        assertThatCode(() -> service.checkIfLocked("user.test")).doesNotThrowAnyException();
    }

    @Test
    void shouldNotLockUserBeforeMaximumAttemptsReached() {
        String username = "user.test";

        service.loginFailed(username);
        service.loginFailed(username);

        assertThatCode(() -> service.checkIfLocked(username)).doesNotThrowAnyException();
    }

    @Test
    void shouldLockUserAfterThreeFailedAttempts() {
        String username = "user.test";

        service.loginFailed(username);
        service.loginFailed(username);
        service.loginFailed(username);

        assertThatThrownBy(() -> service.checkIfLocked(username))
                .isInstanceOf(LockedException.class)
                .hasMessage("User user.test is locked due to too many failed login attempts");
    }

    @Test
    void shouldUnlockUserAfterSuccessfulLogin() {
        String username = "user.test";

        service.loginFailed(username);
        service.loginFailed(username);
        service.loginFailed(username);
        service.loginSuccess(username);

        assertThatCode(() -> service.checkIfLocked(username)).doesNotThrowAnyException();
    }

    @Test
    void shouldResetFailedAttemptsAfterSuccessfulLogin() {
        String username = "user.test";

        service.loginFailed(username);
        service.loginSuccess(username);
        service.loginFailed(username);
        service.loginFailed(username);

        assertThatCode(() -> service.checkIfLocked(username)).doesNotThrowAnyException();
    }

    @Test
    void shouldLockOnlySpecificUser() {
        service.loginFailed("user.one");
        service.loginFailed("user.one");
        service.loginFailed("user.one");

        assertThatThrownBy(() -> service.checkIfLocked("user.one")).isInstanceOf(LockedException.class);
        assertThatCode(() -> service.checkIfLocked("user.two")).doesNotThrowAnyException();
    }
}