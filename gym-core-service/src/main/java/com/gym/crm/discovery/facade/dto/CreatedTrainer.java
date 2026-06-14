package com.gym.crm.discovery.facade.dto;

import com.gym.crm.discovery.model.Trainer;

public record CreatedTrainer(Trainer trainer, String rawPassword) {
}
