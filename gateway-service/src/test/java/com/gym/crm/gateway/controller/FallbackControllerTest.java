package com.gym.crm.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FallbackControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void gymCoreFallback_shouldReturn503_andDefaultMessage() {
        webTestClient.get()
                .uri("/fallback/gym-core-service")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.error").isEqualTo("Service temporarily unavailable")
                .jsonPath("$.service").isEqualTo("gym-core-service")
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.txId").doesNotExist();
    }

    @Test
    void workloadFallback_shouldReturn503_andDefaultMessage() {
        webTestClient.get()
                .uri("/fallback/workload-service")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.error").isEqualTo("Service temporarily unavailable")
                .jsonPath("$.service").isEqualTo("workload-service")
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.txId").doesNotExist();
    }
}