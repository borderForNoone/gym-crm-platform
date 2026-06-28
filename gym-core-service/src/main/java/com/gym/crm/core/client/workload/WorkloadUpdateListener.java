package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadUpdateListener {
    private final WorkloadEventPublisher workloadEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkloadUpdate(WorkloadUpdateEvent event) {
        for (TrainerWorkloadRequest request : event.requests()) {
            publishSafely(request);
        }
    }

    private void publishSafely(TrainerWorkloadRequest request) {
        try {
            workloadEventPublisher.publish(request);
        } catch (Exception exception) {
            log.error("Failed to publish workload event for trainer={}, action={}, message={}",
                    request.getTrainerUsername(), request.getActionType(), exception.getMessage());
        }
    }
}