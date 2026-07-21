package com.gym.crm.core.security.impl;

import com.gym.crm.core.security.BruteForceProtectionService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("automation")
public class InMemoryBruteForceProtectionService implements BruteForceProtectionService {
    private static final int MAX_ATTEMPTS = 3;

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, Boolean> lockedUsers = new ConcurrentHashMap<>();

    @Override
    public void loginSuccess(String username) {
        attempts.remove(username);
        lockedUsers.remove(username);
    }

    @Override
    public void loginFailed(String username) {
        int count = attempts.merge(username, 1, Integer::sum);

        if (count >= MAX_ATTEMPTS) {
            lockedUsers.put(username, true);
        }
    }

    @Override
    public void checkIfLocked(String username) {
        if (Boolean.TRUE.equals(lockedUsers.get(username))) {
            throw new org.springframework.security.authentication.LockedException("User %s is locked due to too many failed login attempts".formatted(username));
        }
    }
}
