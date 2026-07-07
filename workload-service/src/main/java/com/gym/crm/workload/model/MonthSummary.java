package com.gym.crm.workload.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthSummary {
    @Field("month")
    private Integer month;
    @Field("trainingSummaryDuration")
    private Integer trainingSummaryDuration;
}
