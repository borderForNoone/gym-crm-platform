package com.gym.crm.workload.service;

import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;

public interface TrainerWorkloadService {
    void updateTrainerWorkload(TrainerWorkloadRequest request);

    int getMonthlyWorkload(String username, int year, int month);
}
