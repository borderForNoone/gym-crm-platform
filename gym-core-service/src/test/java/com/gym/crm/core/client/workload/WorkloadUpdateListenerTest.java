package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WorkloadUpdateListenerTest {
    private static final String FIRST_USERNAME = "billy.herrington";
    private static final String SECOND_USERNAME = "ricardo.milos";

    @Mock
    private WorkloadEventPublisher workloadEventPublisher;

    private WorkloadUpdateListener listener() {
        return new WorkloadUpdateListener(workloadEventPublisher);
    }

    @Test
    void onWorkloadUpdate_shouldPublishEachRequest_whenEventHasMultipleRequests() {
        WorkloadUpdateListener listener = listener();
        TrainerWorkloadRequest first = buildRequest(FIRST_USERNAME);
        TrainerWorkloadRequest second = buildRequest(SECOND_USERNAME);
        WorkloadUpdateEvent event = new WorkloadUpdateEvent(List.of(first, second));

        listener.onWorkloadUpdate(event);

        verify(workloadEventPublisher).publish(first);
        verify(workloadEventPublisher).publish(second);
    }

    @Test
    void onWorkloadUpdate_shouldPublishRequestsInOrder() {
        WorkloadUpdateListener listener = listener();
        TrainerWorkloadRequest first = buildRequest(FIRST_USERNAME);
        TrainerWorkloadRequest second = buildRequest(SECOND_USERNAME);
        WorkloadUpdateEvent event = new WorkloadUpdateEvent(List.of(first, second));

        listener.onWorkloadUpdate(event);

        InOrder inOrder = inOrder(workloadEventPublisher);
        inOrder.verify(workloadEventPublisher).publish(first);
        inOrder.verify(workloadEventPublisher).publish(second);
    }

    @Test
    void onWorkloadUpdate_shouldDoNothing_whenEventHasNoRequests() {
        WorkloadUpdateListener listener = listener();
        WorkloadUpdateEvent event = new WorkloadUpdateEvent(List.of());

        listener.onWorkloadUpdate(event);

        verifyNoInteractions(workloadEventPublisher);
    }

    @Test
    void onWorkloadUpdate_shouldContinuePublishingRemainingRequests_whenOnePublishFails() {
        WorkloadUpdateListener listener = listener();
        TrainerWorkloadRequest failing = buildRequest(FIRST_USERNAME);
        TrainerWorkloadRequest succeeding = buildRequest(SECOND_USERNAME);
        WorkloadUpdateEvent event = new WorkloadUpdateEvent(List.of(failing, succeeding));
        doThrow(new RuntimeException("broker unavailable")).when(workloadEventPublisher).publish(failing);

        listener.onWorkloadUpdate(event);

        verify(workloadEventPublisher).publish(failing);
        verify(workloadEventPublisher).publish(succeeding);
    }

    @Test
    void onWorkloadUpdate_shouldNotPropagateException_whenPublishFailsForEveryRequest() {
        WorkloadUpdateListener listener = listener();
        TrainerWorkloadRequest request = buildRequest(FIRST_USERNAME);
        WorkloadUpdateEvent event = new WorkloadUpdateEvent(List.of(request));
        doThrow(new RuntimeException("broker unavailable")).when(workloadEventPublisher).publish(any());

        listener.onWorkloadUpdate(event);

        verify(workloadEventPublisher, times(1)).publish(request);
    }

    @Test
    void onWorkloadUpdate_shouldNotPublishAnything_whenPublisherIsNeverInvokedForEmptyEvent() {
        WorkloadUpdateListener listener = listener();
        WorkloadUpdateEvent event = new WorkloadUpdateEvent(List.of());

        listener.onWorkloadUpdate(event);

        verify(workloadEventPublisher, never()).publish(any());
    }

    private TrainerWorkloadRequest buildRequest(String username) {
        return new TrainerWorkloadRequest(username,
                "Billy",
                "Herrington",
                true,
                LocalDate.of(2026, Month.JUNE, 10),
                60,
                ActionType.ADD);
    }
}