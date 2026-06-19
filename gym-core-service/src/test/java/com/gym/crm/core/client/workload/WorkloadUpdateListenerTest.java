package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkloadUpdateListenerTest {

    private static final String FIRST_USERNAME = "billy.herrington";
    private static final String SECOND_USERNAME = "ricardo.milos";

    @Mock
    private WorkloadServiceClient clientService;

    @InjectMocks
    private WorkloadUpdateListener listener;

    @Test
    void handle_shouldSendAllRequestsToWorkloadClient() {
        TrainerWorkloadRequest first = new TrainerWorkloadRequest().trainerUsername(FIRST_USERNAME);
        TrainerWorkloadRequest second = new TrainerWorkloadRequest().trainerUsername(SECOND_USERNAME);

        listener.handle(new WorkloadUpdateEvent(List.of(first, second)));

        verify(clientService).updateTrainerWorkload(first);
        verify(clientService).updateTrainerWorkload(second);
    }

    @Test
    void handle_whenClientFails_shouldNotThrow() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest().trainerUsername(FIRST_USERNAME);

        doThrow(new RuntimeException("workload unavailable")).when(clientService).updateTrainerWorkload(request);

        assertDoesNotThrow(() -> listener.handle(new WorkloadUpdateEvent(List.of(request))));
        verify(clientService).updateTrainerWorkload(request);
    }
}
