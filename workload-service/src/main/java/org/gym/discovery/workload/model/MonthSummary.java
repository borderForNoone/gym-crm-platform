package org.gym.discovery.workload.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MonthSummary {
    private Integer month;
    private Integer trainingSummaryDuration;
}
