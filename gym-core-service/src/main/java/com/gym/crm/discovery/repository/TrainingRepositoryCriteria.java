package com.gym.crm.discovery.repository;

import com.gym.crm.discovery.model.Training;
import com.gym.crm.discovery.search.filter.TraineeTrainingFilter;
import com.gym.crm.discovery.search.filter.TrainerTrainingFilter;

import java.util.List;

public interface TrainingRepositoryCriteria {
    List<Training> findByTraineeCriteria(TraineeTrainingFilter filter);

    List<Training> findByTrainerCriteria(TrainerTrainingFilter filter);
}