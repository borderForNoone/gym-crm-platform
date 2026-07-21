package com.gym.crm.core.security.impl;

import com.gym.crm.core.security.TokenBlacklistService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("automation")
public class InMemoryTokenBlacklistService implements TokenBlacklistService {
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    @Override
    public void blacklist(String token) {
        blacklist.add(token);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
