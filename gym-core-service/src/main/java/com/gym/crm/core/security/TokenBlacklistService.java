package com.gym.crm.core.security;

public interface TokenBlacklistService {
    void blacklist(String token);

    boolean isBlacklisted(String token);
}
