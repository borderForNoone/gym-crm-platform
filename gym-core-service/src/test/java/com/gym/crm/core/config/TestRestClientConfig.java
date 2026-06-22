package com.gym.crm.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestClient;

@TestConfiguration
@PropertySource(value = "classpath:application-test.yml")
public class TestRestClientConfig {
    @Value("${workload.service.base-url}")
    private String baseUrl;

    @Bean
    public RestClient workloadRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
