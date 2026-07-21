package com.gym.crm.workload.security;

public interface TokenBlacklistService {
    void blacklist(String token);

    boolean isBlacklisted(String token);
}
