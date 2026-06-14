package com.gym.crm.discovery.service;

import com.gym.crm.discovery.facade.dto.TrainingResponseDTO;
import com.gym.crm.discovery.facade.dto.TrainingTypeDTO;
import com.gym.crm.discovery.model.Training;
import com.gym.crm.discovery.search.filter.TraineeTrainingFilter;
import com.gym.crm.discovery.search.filter.TrainerTrainingFilter;

import java.util.List;

public interface TrainingService {
    Training create(Training training);

    List<TrainingResponseDTO> getTraineeTrainings(TraineeTrainingFilter filter);

    List<TrainingResponseDTO> getTrainerTrainings(TrainerTrainingFilter filter);

    List<TrainingTypeDTO> getAllTrainingTypes();
}
