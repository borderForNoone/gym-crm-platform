package com.gym.crm.workload.service;

import gym.crm.platform.workload.openapi.ActionType;
import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.Month;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TrainerWorkloadServiceTest {
    private static final String USERNAME = "billy.herrington";
    private static final String FIRST_NAME = "Billy";
    private static final String LAST_NAME = "Herrington";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;

    @Autowired
    private TrainerWorkloadServiceImpl service;

    @Test
    void updateTrainerWorkload_shouldAddDuration_whenActionTypeAdd() {
        service.updateTrainerWorkload(createRequest(60, ActionType.ADD));

        int result = service.getMonthlyWorkload(USERNAME, YEAR, MONTH);

        assertEquals(60, result);
    }

    @Test
    void updateTrainerWorkload_shouldSumDuration_whenAddCalledTwice() {
        service.updateTrainerWorkload(createRequest(60, ActionType.ADD));
        service.updateTrainerWorkload(createRequest(30, ActionType.ADD));

        int result = service.getMonthlyWorkload(USERNAME, YEAR, MONTH);

        assertEquals(90, result);
    }

    @Test
    void updateTrainerWorkload_shouldSubtractDuration_whenActionTypeDelete() {
        service.updateTrainerWorkload(createRequest(90, ActionType.ADD));
        service.updateTrainerWorkload(createRequest(30, ActionType.DELETE));

        int result = service.getMonthlyWorkload(USERNAME, YEAR, MONTH);

        assertEquals(60, result);
    }

    @Test
    void updateTrainerWorkload_shouldNotSetNegativeDuration_whenDeleteGreaterThanCurrent() {
        service.updateTrainerWorkload(createRequest(30, ActionType.ADD));
        service.updateTrainerWorkload(createRequest(60, ActionType.DELETE));

        int result = service.getMonthlyWorkload(USERNAME, YEAR, MONTH);

        assertEquals(0, result);
    }

    @Test
    void updateTrainerWorkload_shouldNotChangeOtherMonth_whenAddingDurationToDifferentMonth() {
        TrainerWorkloadRequest nextMonthRequest = createRequest(30, ActionType.ADD).trainingDate(LocalDate.of(YEAR, Month.JULY, 10));

        service.updateTrainerWorkload(createRequest(60, ActionType.ADD));
        service.updateTrainerWorkload(nextMonthRequest);

        int actualJuneWorkload = service.getMonthlyWorkload(USERNAME, YEAR, MONTH);
        int actualJulyWorkload = service.getMonthlyWorkload(USERNAME, YEAR, 7);
        assertEquals(60, actualJuneWorkload);
        assertEquals(30, actualJulyWorkload);
    }

    @Test
    void getMonthlyWorkload_shouldReturnZero_whenTrainerExistsButMonthNotExists() {
        service.updateTrainerWorkload(createRequest(60, ActionType.ADD));

        int actual = service.getMonthlyWorkload(USERNAME, YEAR, 7);

        assertEquals(0, actual);
    }

    @Test
    void getMonthlyWorkload_shouldThrowException_whenTrainerNotFound() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> service.getMonthlyWorkload("unknown.user", YEAR, MONTH));

        assertEquals("Trainer workload not found: unknown.user", exception.getMessage());
    }

    private TrainerWorkloadRequest createRequest(int duration, ActionType actionType) {
        return new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .isActive(true)
                .trainingDate(LocalDate.of(YEAR, MONTH, 10))
                .trainingDuration(duration)
                .actionType(actionType);
    }
}
