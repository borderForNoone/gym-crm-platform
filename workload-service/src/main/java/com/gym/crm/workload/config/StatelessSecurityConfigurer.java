package com.gym.crm.workload.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

public class StatelessSecurityConfigurer extends AbstractHttpConfigurer<StatelessSecurityConfigurer, HttpSecurity> {
    @Override
    public void init(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
    }
}
