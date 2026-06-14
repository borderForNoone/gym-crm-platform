package com.gym.crm.platform.repository;

import com.gym.crm.platform.model.Training;
import com.gym.crm.platform.search.filter.TraineeTrainingFilter;
import com.gym.crm.platform.search.filter.TrainerTrainingFilter;

import java.util.List;

public interface TrainingRepositoryCriteria {
    List<Training> findByTraineeCriteria(TraineeTrainingFilter filter);

    List<Training> findByTrainerCriteria(TrainerTrainingFilter filter);
}