package com.gym.crm.workload.repository;

import com.gym.crm.workload.model.TrainerWorkload;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerWorkloadRepositoryTest {
    private static final String USERNAME = "billy.herrington";
    private static final String UNKNOWN_USERNAME = "unknown.user";

    private final TrainerWorkloadRepositoryImpl repository = new TrainerWorkloadRepositoryImpl();

    @Test
    void save_shouldStoreTrainerWorkload() {
        TrainerWorkload expected = buildTrainer();

        TrainerWorkload actual = repository.save(expected);

        assertSame(expected, actual);
        assertTrue(repository.existsByUsername(USERNAME));
    }

    @Test
    void findByUsername_shouldReturnTrainerWorkload_whenExists() {
        TrainerWorkload workload = buildTrainer();
        repository.save(workload);

        Optional<TrainerWorkload> actual = repository.findByUsername(USERNAME);

        assertTrue(actual.isPresent());
        assertEquals(USERNAME, actual.get().getTrainerUsername());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotExists() {
        Optional<TrainerWorkload> actual = repository.findByUsername("unknown.user");

        assertTrue(actual.isEmpty());
    }

    @Test
    void existsByUsername_shouldReturnFalse_whenNotExists() {
        boolean actual = repository.existsByUsername(UNKNOWN_USERNAME);

        assertFalse(actual);
    }

    private TrainerWorkload buildTrainer() {
        return new TrainerWorkload(USERNAME, "Billy", "Herrington", true, new ArrayList<>());
    }
}
