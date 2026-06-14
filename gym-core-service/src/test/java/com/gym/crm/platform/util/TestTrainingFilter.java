package com.gym.crm.platform.util;

import com.gym.crm.platform.search.filter.TrainingFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TestTrainingFilter extends TrainingFilter {
}