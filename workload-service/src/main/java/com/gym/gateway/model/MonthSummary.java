package com.gym.gateway.model;


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
