package com.gym.crm.bdd.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayloadField {
    USERNAME("username"),
    PASSWORD("password"),
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    DATE_OF_BIRTH("dateOfBirth"),
    ADDRESS("address"),
    SPECIALIZATION("specialization"),
    TRAINEE_USERNAME("traineeUsername"),
    TRAINER_USERNAME("trainerUsername"),
    TRAINING_NAME("trainingName"),
    TRAINING_DATE("trainingDate"),
    TRAINING_DURATION("trainingDuration"),
    TRAINER_FIRST_NAME("trainerFirstName"),
    TRAINER_LAST_NAME("trainerLastName"),
    IS_ACTIVE("isActive"),
    ACTION_TYPE("actionType");

    private final String value;
}
