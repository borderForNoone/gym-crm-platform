package com.gym.crm.core.security;

public interface BruteForceProtectionService {
    void loginSuccess(String username);

    void loginFailed(String username);

    void checkIfLocked(String username);
}