package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadServiceClient {
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String MDC_TRANSACTION_ID_KEY = "transactionId";
    private static final String TRAINER_WORKLOADS_PATH = "/trainer-workloads";

    private final RestClient client;
    private final ServiceTokenProvider serviceTokenProvider;

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
        } catch (RestClientException exception) {
            log.error("workload-service call failed for trainer={} action={} txId={} message={}", request.getTrainerUsername(), request.getActionType(),
                    transactionId, exception.getMessage());

            throw exception;
        }
    }
}
