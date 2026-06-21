package com.gym.crm.gateway.controller;

import com.gym.crm.gateway.dto.FallbackResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

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

    private static final String ERROR_MESSAGE = "Service temporarily unavailable";

    @RequestMapping(value = "/{serviceName}", method = {GET, POST, PUT, PATCH, DELETE})
    public ResponseEntity<FallbackResponse> fallback(@PathVariable String serviceName, ServerWebExchange exchange) {
        Throwable exception = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        Set<URI> originalUris = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);

        log.info("Circuit breaker fallback triggered for service={}, method={}, path={}, originalUris={}, reason={}",
                serviceName,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(),
                originalUris,
                exception == null ? "unknown" : exception.getMessage());

        FallbackResponse response = new FallbackResponse(Instant.now(), HttpStatus.SERVICE_UNAVAILABLE.value(), ERROR_MESSAGE, serviceName);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}