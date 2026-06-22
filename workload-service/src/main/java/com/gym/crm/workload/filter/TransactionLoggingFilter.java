package com.gym.crm.workload.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class TransactionLoggingFilter extends OncePerRequestFilter {
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String MDC_TRANSACTION_ID_KEY = "transactionId";
    private static final int ERROR_STATUS_THRESHOLD = 400;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String transactionId = currentOrNewTransactionId(request);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);
        MDC.put(MDC_TRANSACTION_ID_KEY, transactionId);

        RequestTimer timer = RequestTimer.start();
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            log.info(">>> [{} {}] ip={} auth={} txId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    SensitiveDataMasker.maskAuthHeader(request.getHeader("Authorization")),
                    transactionId);

            filterChain.doFilter(request, wrappedResponse);

            logCompletion(request, wrappedResponse.getStatus(), timer.elapsedMillis());
        } finally {
            wrappedResponse.copyBodyToResponse();
            MDC.remove(MDC_TRANSACTION_ID_KEY);
        }
    }

    private String currentOrNewTransactionId(HttpServletRequest request) {
        String header = request.getHeader(TRANSACTION_ID_HEADER);

        return (header == null || header.isBlank()) ? UUID.randomUUID().toString() : header;
    }

    private void logCompletion(HttpServletRequest request, int status, long durationMillis) {
        String message = "<<< [{} {}] status={} duration={}ms";
        Object[] args = {request.getMethod(), request.getRequestURI(), status, durationMillis};

        if (status >= ERROR_STATUS_THRESHOLD) {
            log.warn(message, args);
        } else {
            log.info(message, args);
        }
    }

    private record RequestTimer(long startedAtMillis) {
        static RequestTimer start() {
            return new RequestTimer(System.currentTimeMillis());
        }

        long elapsedMillis() {
            return System.currentTimeMillis() - startedAtMillis;
        }
    }
}