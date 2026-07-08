package com.gym.crm.workload.repository;

import com.gym.crm.workload.config.MongoContainerTestConfig;
import com.gym.crm.workload.model.MonthSummary;
import com.gym.crm.workload.model.TrainerWorkload;
import com.gym.crm.workload.model.YearSummary;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
class TrainerWorkloadRepositoryTest extends MongoContainerTestConfig<TrainerWorkloadRepository> {
    private static final String USERNAME = "billy.herrington";
    private static final String UNKNOWN_USERNAME = "unknown.user";
    private static final String FIRST_NAME = "Billy";
    private static final String LAST_NAME = "Herrington";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DURATION = 60;

    @Test
    void save_shouldStoreTrainerWorkload() {
        TrainerWorkload expected = buildTrainerWorkload();
        TrainerWorkload saved = repository.save(expected);

        Optional<TrainerWorkload> actual = repository.findByTrainerUsername(USERNAME);

        assertThat(saved.getTrainerUsername()).isEqualTo(USERNAME);
        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @Test
    void findByTrainerUsername_shouldReturnTrainerWorkload_whenExists() {
        TrainerWorkload expected = buildTrainerWorkload();
        repository.save(expected);

        Optional<TrainerWorkload> actual = repository.findByTrainerUsername(USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void findByTrainerUsername_shouldReturnEmpty_whenNotExists() {
        Optional<TrainerWorkload> actual = repository.findByTrainerUsername(UNKNOWN_USERNAME);

        assertThat(actual).isEmpty();
    }

    @Test
    void existsByTrainerUsername_shouldReturnTrue_whenExists() {
        TrainerWorkload expected = buildTrainerWorkload();
        repository.save(expected);

        boolean actual = repository.existsByTrainerUsername(USERNAME);

        assertThat(actual).isTrue();
    }

    @Test
    void existsByTrainerUsername_shouldReturnFalse_whenNotExists() {
        boolean actual = repository.existsByTrainerUsername(UNKNOWN_USERNAME);

        assertThat(actual).isFalse();
    }

    private TrainerWorkload buildTrainerWorkload() {
        MonthSummary monthSummary = new MonthSummary(MONTH, DURATION);
        YearSummary yearSummary = new YearSummary(YEAR, List.of(monthSummary));

        return new TrainerWorkload(USERNAME, FIRST_NAME, LAST_NAME, true, List.of(yearSummary));
    }
}
