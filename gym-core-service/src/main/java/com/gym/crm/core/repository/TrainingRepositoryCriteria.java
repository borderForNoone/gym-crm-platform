package com.gym.crm.core.repository;

import com.gym.crm.core.model.Training;
import com.gym.crm.core.search.filter.TraineeTrainingFilter;
import com.gym.crm.core.search.filter.TrainerTrainingFilter;

import java.util.List;

public interface TrainingRepositoryCriteria {
    List<Training> findByTraineeCriteria(TraineeTrainingFilter filter);

    List<Training> findByTrainerCriteria(TrainerTrainingFilter filter);
}