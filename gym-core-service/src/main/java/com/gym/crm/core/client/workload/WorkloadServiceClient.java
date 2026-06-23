package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadServiceClient {
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String MDC_TRANSACTION_ID_KEY = "transactionId";
    private static final String TRAINER_WORKLOADS_PATH = "/trainer-workloads";
    private static final String CIRCUIT_BREAKER_NAME = "workloadServiceClient";

    private final RestClient client;
    private final ServiceTokenProvider serviceTokenProvider;

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "updateTrainerWorkloadFallback")
    public void updateTrainerWorkload(TrainerWorkloadRequest request) {
        String transactionId = MDC.get(MDC_TRANSACTION_ID_KEY);

        log.info("Calling workload-service [PUT {}] action={} trainer={} txId={}",
                TRAINER_WORKLOADS_PATH, request.getActionType(), request.getTrainerUsername(), transactionId);

        try {
            client.put()
                    .uri(TRAINER_WORKLOADS_PATH)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceTokenProvider.getToken())
                    .header(TRANSACTION_ID_HEADER, transactionId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("workload-service responded 200 OK for trainer={} action={} txId={}", request.getTrainerUsername(), request.getActionType(), transactionId);
        } catch (ResourceAccessException timeoutOrConnectionException) {
            log.error("workload-service timed out or was unreachable for trainer={} action={} txId={} message={}",
                    request.getTrainerUsername(), request.getActionType(), transactionId, timeoutOrConnectionException.getMessage());

            throw timeoutOrConnectionException;
        } catch (RestClientException exception) {
            log.error("workload-service call failed for trainer={} action={} txId={} message={}", request.getTrainerUsername(), request.getActionType(),
                    transactionId, exception.getMessage());

            throw exception;
        }
    }

    @SuppressWarnings("unused")
    private void updateTrainerWorkloadFallback(TrainerWorkloadRequest request, Throwable throwable) {
        String transactionId = MDC.get(MDC_TRANSACTION_ID_KEY);

        log.error("workload-service unavailable, falling back. trainer={} action={} txId={} reason={}",
                request.getTrainerUsername(), request.getActionType(), transactionId, throwable.getMessage());
    }
}