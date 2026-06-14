package com.gym.crm.platform.search.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TraineeTrainingFilter extends TrainingFilter {
    private String trainingTypeName;
}
