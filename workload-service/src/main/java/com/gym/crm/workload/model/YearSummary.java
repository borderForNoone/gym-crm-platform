package com.gym.crm.workload.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class YearSummary {
    @Field("year")
    private Integer year;
    @Field("months")
    private List<MonthSummary> months;
}