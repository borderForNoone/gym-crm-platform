package com.gym.crm.discovery.search.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TraineeTrainingFilter extends TrainingFilter {
    private String trainingTypeName;
}
