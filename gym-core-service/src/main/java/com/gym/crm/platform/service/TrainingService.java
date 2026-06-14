package com.gym.crm.platform.service;

import com.gym.crm.platform.facade.dto.TrainingResponseDTO;
import com.gym.crm.platform.facade.dto.TrainingTypeDTO;
import com.gym.crm.platform.model.Training;
import com.gym.crm.platform.search.filter.TraineeTrainingFilter;
import com.gym.crm.platform.search.filter.TrainerTrainingFilter;

import java.util.List;

public interface TrainingService {
    Training create(Training training);

    List<TrainingResponseDTO> getTraineeTrainings(TraineeTrainingFilter filter);

    List<TrainingResponseDTO> getTrainerTrainings(TrainerTrainingFilter filter);

    List<TrainingTypeDTO> getAllTrainingTypes();
}
