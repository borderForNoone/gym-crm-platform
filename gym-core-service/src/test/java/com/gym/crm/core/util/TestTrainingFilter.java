package com.gym.crm.core.util;

import com.gym.crm.core.search.filter.TrainingFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TestTrainingFilter extends TrainingFilter {
}