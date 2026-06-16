package com.gym.crm.core.facade.dto;

import com.gym.crm.core.model.Trainer;

public record CreatedTrainer(Trainer trainer, String rawPassword) {
}
