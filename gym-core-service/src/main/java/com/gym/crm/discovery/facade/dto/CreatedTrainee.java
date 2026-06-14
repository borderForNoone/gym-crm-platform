package com.gym.crm.discovery.facade.dto;

import com.gym.crm.discovery.model.Trainee;

public record CreatedTrainee(Trainee trainee, String rawPassword) {
}