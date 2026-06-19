package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkloadRequestMapperTest {
    private static final String USERNAME = "billy.herrington";
    private static final String FIRST_NAME = "Billy";
    private static final String LAST_NAME = "Herrington";

    private final WorkloadRequestMapper mapper = new WorkloadRequestMapper();

    @Test
    void toRequest_shouldMapTrainingToWorkloadRequest() {
        User user = User.builder()
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(true)
                .build();
        Trainer trainer = Trainer.builder()
                .user(user)
                .build();
        Training training = Training.builder()
                .trainer(trainer)
                .trainingDate(LocalDate.of(2026, Month.JUNE, 10))
                .trainingDuration(60)
                .build();

        TrainerWorkloadRequest actual = mapper.toRequest(training, ActionType.ADD);

        assertEquals(USERNAME, actual.getTrainerUsername());
        assertEquals(FIRST_NAME, actual.getTrainerFirstName());
        assertEquals(LAST_NAME, actual.getTrainerLastName());
        assertTrue(actual.getIsActive());
        assertEquals(LocalDate.of(2026, Month.JUNE, 10), actual.getTrainingDate());
        assertEquals(60, actual.getTrainingDuration());
        assertEquals(ActionType.ADD, actual.getActionType());
    }
}
