package com.gym.crm.platform.facade.dto;

import com.gym.crm.platform.model.Trainer;

public record CreatedTrainer(Trainer trainer, String rawPassword) {}
