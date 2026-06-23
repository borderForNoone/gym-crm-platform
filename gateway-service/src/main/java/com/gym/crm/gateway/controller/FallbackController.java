package com.gym.crm.gateway.controller;

import com.gym.crm.gateway.dto.FallbackResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.net.ConnectException;
import java.net.URI;
import java.time.Instant;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    private static final String DEFAULT_MESSAGE = "Service temporarily unavailable";

    @RequestMapping(value = "/{serviceName}", method = {GET, POST, PUT, PATCH, DELETE})
    public ResponseEntity<FallbackResponse> fallback(@PathVariable String serviceName, ServerWebExchange exchange) {
        Throwable throwable = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);

        Set<URI> originalUris = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);

        String txId = extractTxId(exchange);

        log.info("Fallback triggered txId={}, service={}, method={}, path={}, uris={}", txId, serviceName, exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(), originalUris);

        return handleException(serviceName, throwable, txId);
    }

    private ResponseEntity<FallbackResponse> handleException(String serviceName, Throwable throwable, String txId) {
        HttpStatus httpStatus;
        int status;
        String message;

        if (throwable instanceof TimeoutException) {
            log.warn("TIMEOUT txId={}, service={}", txId, serviceName);

            httpStatus = HttpStatus.GATEWAY_TIMEOUT;
            status = 504;
            message = "Timeout: " + serviceName + " did not respond within 3s";
        } else if (throwable instanceof ConnectException) {
            log.warn("CONNECT ERROR txId={}, service={}", txId, serviceName);

            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
            status = 503;
            message = "Connection error: Cannot connect to " + serviceName;
        } else {
            log.warn("UNKNOWN ERROR txId={}, service={}, error={}", txId, serviceName, throwable != null ? throwable.getClass().getSimpleName() : "null");

            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
            status = 503;
            message = DEFAULT_MESSAGE;
        }

        FallbackResponse response = new FallbackResponse(Instant.now(), status, message, serviceName, txId);

        return ResponseEntity.status(httpStatus).body(response);
    }

    private String extractTxId(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("txId");
    }
}