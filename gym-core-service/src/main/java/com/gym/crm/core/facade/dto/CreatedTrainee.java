package com.gym.crm.core.facade.dto;

import com.gym.crm.core.model.Trainee;

public record CreatedTrainee(Trainee trainee, String rawPassword) {
}