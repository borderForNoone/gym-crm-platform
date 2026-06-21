package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class WorkloadServiceClient {
    private final RestClient client;
    private final ServiceTokenProvider serviceTokenProvider;

    public void updateTrainerWorkload(TrainerWorkloadRequest request) {
        client.put()
                .uri("/trainer-workloads")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceTokenProvider.getToken())
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
