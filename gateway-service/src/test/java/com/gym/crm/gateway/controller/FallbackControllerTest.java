package com.gym.crm.gateway.controller;

import com.gym.crm.gateway.dto.FallbackResponse;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import java.net.ConnectException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class FallbackControllerTest {

    private final FallbackController controller = new FallbackController();

    @Test
    void shouldReturn504ForTimeoutException() {
        ServerWebExchange exchange = createExchange(
                new TimeoutException("timeout"),
                "tx-timeout"
        );

        var response = controller.fallback("gym-core-service", exchange);

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, response.getStatusCode());

        FallbackResponse body = response.getBody();
        assertNotNull(body);

        assertEquals(504, body.status());
        assertEquals(
                "Timeout: gym-core-service did not respond within 3s",
                body.error()
        );
        assertEquals("gym-core-service", body.service());
        assertEquals("tx-timeout", body.txId());
        assertNotNull(body.timestamp());
    }

    @Test
    void shouldReturn503ForConnectException() {
        ServerWebExchange exchange = createExchange(
                new ConnectException("connection refused"),
                "tx-connect"
        );

        var response = controller.fallback("workload-service", exchange);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());

        FallbackResponse body = response.getBody();
        assertNotNull(body);

        assertEquals(503, body.status());
        assertEquals(
                "Connection error: Cannot connect to workload-service",
                body.error()
        );
        assertEquals("workload-service", body.service());
        assertEquals("tx-connect", body.txId());
        assertNotNull(body.timestamp());
    }

    @Test
    void shouldReturn503ForUnknownException() {
        ServerWebExchange exchange = createExchange(
                new RuntimeException("unexpected"),
                "tx-runtime"
        );

        var response = controller.fallback("gym-core-service", exchange);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());

        FallbackResponse body = response.getBody();
        assertNotNull(body);

        assertEquals(503, body.status());
        assertEquals("Service temporarily unavailable", body.error());
        assertEquals("gym-core-service", body.service());
        assertEquals("tx-runtime", body.txId());
        assertNotNull(body.timestamp());
    }

    @Test
    void shouldReturn503WhenThrowableIsNull() {
        ServerWebExchange exchange = createExchange(null, null);

        var response = controller.fallback("workload-service", exchange);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());

        FallbackResponse body = response.getBody();
        assertNotNull(body);

        assertEquals(503, body.status());
        assertEquals("Service temporarily unavailable", body.error());
        assertEquals("workload-service", body.service());
        assertNull(body.txId());
        assertNotNull(body.timestamp());
    }

    private ServerWebExchange createExchange(Throwable throwable, String txId) {
        MockServerHttpRequest.BaseBuilder<?> requestBuilder =
                MockServerHttpRequest.get("/test");

        if (txId != null) {
            requestBuilder.header("txId", txId);
        }

        MockServerWebExchange exchange =
                MockServerWebExchange.from(requestBuilder.build());

        if (throwable != null) {
            exchange.getAttributes().put(
                    ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR,
                    throwable
            );
        }

        return exchange;
    }
}