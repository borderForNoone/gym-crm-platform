package com.gym.crm.workload.security.imp;

import com.gym.crm.workload.security.TokenBlacklistService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("automation")
public class NoOpTokenBlacklistService implements TokenBlacklistService {
    @Override
    public void blacklist(String token) {
    }

    @Override
    public boolean isBlacklisted(String token) {
        return false;
    }
}
