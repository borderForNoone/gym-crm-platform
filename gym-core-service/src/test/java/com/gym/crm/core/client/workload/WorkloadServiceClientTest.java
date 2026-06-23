package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import com.gym.crm.core.config.TestRestClientConfig;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterAutoConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JAutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest
@ImportAutoConfiguration({AopAutoConfiguration.class, CircuitBreakerAutoConfiguration.class, TimeLimiterAutoConfiguration.class, Resilience4JAutoConfiguration.class})
@ContextConfiguration(classes = {
        WorkloadServiceClient.class,
        ServiceTokenProvider.class,
        TestRestClientConfig.class
})
class WorkloadServiceClientTest {
    private static final String USERNAME = "billy.herrington";
    private static final String FIRST_NAME = "Billy";
    private static final String LAST_NAME = "Herrington";

    @Autowired
    private ServiceTokenProvider serviceTokenProvider;
    @Autowired
    private MockRestServiceServer server;
    @Autowired
    private WorkloadServiceClient client;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString("test-secret-key-must-be-long-enough-for-hmac-sha256".getBytes());
        ReflectionTestUtils.setField(serviceTokenProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(serviceTokenProvider, "serviceTokenExpirationMs", 300_000L);
    }

    @Test
    void updateTrainerWorkload_shouldSendPutRequest_withBearerAuthorizationHeader() {
        TrainerWorkloadRequest request = buildRequest();
        server.expect(requestTo("/trainer-workloads"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header("Authorization", Matchers.matchesPattern("Bearer .+")))
                .andExpect(jsonPath("$.trainerUsername").value(USERNAME))
                .andExpect(jsonPath("$.trainingDuration").value(60))
                .andExpect(jsonPath("$.actionType").value("ADD"))
                .andRespond(withSuccess());

        client.updateTrainerWorkload(request);

        server.verify();
    }

    @Test
    void updateTrainerWorkload_shouldNotPropagateException_whenWorkloadServiceReturnsServerError() {
        TrainerWorkloadRequest request = buildRequest();
        server.expect(requestTo("/trainer-workloads"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withServerError());

        assertThatNoException().isThrownBy(() -> client.updateTrainerWorkload(request));
    }

    @Test
    void updateTrainerWorkload_shouldNotPropagateException_whenConnectionFails() {
        TrainerWorkloadRequest request = buildRequest();
        server.expect(requestTo("/trainer-workloads"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(httpRequest -> {
                    throw new IOException("connection refused");
                });

        assertThatNoException().isThrownBy(() -> client.updateTrainerWorkload(request));
    }

    private TrainerWorkloadRequest buildRequest() {
        return new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .isActive(true)
                .trainingDate(LocalDate.of(2026, Month.JUNE, 10))
                .trainingDuration(60)
                .actionType(ActionType.ADD);
    }
}