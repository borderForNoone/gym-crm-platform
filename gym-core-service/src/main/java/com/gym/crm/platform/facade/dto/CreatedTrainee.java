package com.gym.crm.platform.facade.dto;

import com.gym.crm.platform.model.Trainee;

public record CreatedTrainee(Trainee trainee, String rawPassword) {}