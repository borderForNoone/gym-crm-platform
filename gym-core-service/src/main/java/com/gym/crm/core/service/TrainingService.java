package com.gym.crm.core.service;

import com.gym.crm.core.facade.dto.TrainingRequestDTO;
import com.gym.crm.core.facade.dto.TrainingResponseDTO;
import com.gym.crm.core.facade.dto.TrainingTypeDTO;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.search.filter.TraineeTrainingFilter;
import com.gym.crm.core.search.filter.TrainerTrainingFilter;

import java.util.List;

public interface TrainingService {
    Training create(TrainingRequestDTO training);

    List<TrainingResponseDTO> getTraineeTrainings(TraineeTrainingFilter filter);

    List<TrainingResponseDTO> getTrainerTrainings(TrainerTrainingFilter filter);

    List<TrainingTypeDTO> getAllTrainingTypes();
}
