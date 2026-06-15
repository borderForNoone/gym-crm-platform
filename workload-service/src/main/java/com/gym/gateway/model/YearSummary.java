package com.gym.discovery.workload.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class YearSummary {
    private Integer year;
    private List<MonthSummary> months;
}