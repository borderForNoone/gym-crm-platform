package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.User;
import org.springframework.stereotype.Component;

@Component
public class WorkloadRequestMapper {
    public TrainerWorkloadRequest toRequest(Training training, ActionType actionType) {
        Trainer trainer = training.getTrainer();
        User user = trainer.getUser();

        return new TrainerWorkloadRequest(user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getIsActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType);
    }
}
