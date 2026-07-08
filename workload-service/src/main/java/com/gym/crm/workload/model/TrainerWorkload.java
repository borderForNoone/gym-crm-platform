package com.gym.crm.workload.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "trainer_workloads")
@CompoundIndex(name = "trainer_name_idx", def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
public class TrainerWorkload {
    @Id
    @Field("trainerUsername")
    private String trainerUsername;
    @Field("trainerFirstName")
    private String trainerFirstName;
    @Field("trainerLastName")
    private String trainerLastName;
    @Field("isActive")
    private Boolean isActive;
    @Field("years")
    private List<YearSummary> years;
}