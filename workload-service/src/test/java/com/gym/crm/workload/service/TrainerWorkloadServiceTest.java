package com.gym.crm.workload.service;

import com.gym.crm.workload.model.MonthSummary;
import com.gym.crm.workload.model.TrainerWorkload;
import com.gym.crm.workload.model.YearSummary;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import gym.crm.platform.workload.openapi.ActionType;
import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.Calendar.MONTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {
    private static final String USERNAME = "billy.herrington";
    private static final String FIRST_NAME = "Billy";
    private static final String LAST_NAME = "Herrington";
    private static final int YEAR = 2026;
    private static final Month TRAINING_MONTH = Month.JUNE;
    private static final Month NEXT_MONTH = Month.JULY;

    @Mock
    private TrainerWorkloadRepository repository;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private TrainerWorkloadServiceImpl service;

    @Test
    void getMonthlyWorkload_shouldReturnDuration_whenWorkloadExists() {
        TrainerWorkload workload = buildTrainerWorkload(60);

        when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(workload));

        int actual = service.getMonthlyWorkload(USERNAME, YEAR, TRAINING_MONTH.getValue());

        assertThat(actual).isEqualTo(60);
        verify(repository).findByTrainerUsername(USERNAME);
    }

    @Test
    void getMonthlyWorkload_shouldThrowException_whenWorkloadDoesNotExist() {
        int month = TRAINING_MONTH.getValue();

        when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> service.getMonthlyWorkload(USERNAME, YEAR, month));

        assertThat(exception.getMessage()).isEqualTo("Trainer workload not found: billy.herrington");
        verify(repository).findByTrainerUsername(USERNAME);
    }

    @Test
    void updateTrainerWorkload_shouldCreateNewWorkloadOnAdd_whenWorkloadDoesNotExist() {
        TrainerWorkloadRequest request = buildRequest(ActionType.ADD, 60);

        when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.empty());

        service.updateTrainerWorkload(request);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());
        TrainerWorkload actual = captor.getValue();
        assertThat(actual.getTrainerUsername()).isEqualTo(USERNAME);
        assertThat(actual.getTrainerFirstName()).isEqualTo(FIRST_NAME);
        assertThat(actual.getTrainerLastName()).isEqualTo(LAST_NAME);
        assertThat(actual.getIsActive()).isTrue();
        assertThat(actual.getYears()).hasSize(1);
        assertThat(actual.getYears().get(0).getYear()).isEqualTo(YEAR);
        assertThat(actual.getYears().get(0).getMonths()).hasSize(1);
        assertThat(getFirstMonth(actual).getMonth()).isEqualTo(TRAINING_MONTH.getValue());
        assertThat(getFirstMonth(actual).getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    void updateTrainerWorkload_shouldUpdateWorkloadOnAdd_whenWorkloadExists() {
        TrainerWorkload workload = buildTrainerWorkload(60);
        TrainerWorkloadRequest request = buildRequest(ActionType.ADD, 30)
                .trainerFirstName("NewFirstName")
                .trainerLastName("NewLastName")
                .isActive(false);

        when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(workload));

        service.updateTrainerWorkload(request);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());
        TrainerWorkload actual = captor.getValue();
        assertThat(actual.getTrainerFirstName()).isEqualTo("NewFirstName");
        assertThat(actual.getTrainerLastName()).isEqualTo("NewLastName");
        assertThat(actual.getIsActive()).isFalse();
        assertThat(getFirstMonth(actual).getTrainingSummaryDuration()).isEqualTo(90);
    }

    @Test
    void updateTrainerWorkload_shouldUpdateWorkloadOnDelete_whenWorkloadExists() {
        TrainerWorkload workload = buildTrainerWorkload(100);
        TrainerWorkloadRequest request = buildRequest(ActionType.DELETE, 40);

        when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(workload));

        service.updateTrainerWorkload(request);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());
        TrainerWorkload actual = captor.getValue();
        assertThat(getFirstMonth(actual).getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    void updateTrainerWorkload_shouldNotSetNegativeDuration_whenDeleteGreaterThanCurrent() {
        TrainerWorkload workload = buildTrainerWorkload(30);
        TrainerWorkloadRequest request = buildRequest(ActionType.DELETE, 60);

        when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(workload));

        service.updateTrainerWorkload(request);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());
        TrainerWorkload actual = captor.getValue();
        assertThat(getFirstMonth(actual).getTrainingSummaryDuration()).isZero();
    }

    @Test
    void updateTrainerWorkload_shouldCreateDifferentMonth_whenTrainingMonthDoesNotExist() {
        TrainerWorkload workload = buildTrainerWorkload(60);
        TrainerWorkloadRequest request = buildRequest(ActionType.ADD, 30)
                .trainingDate(LocalDate.of(YEAR, NEXT_MONTH, 10));

        when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(workload));

        service.updateTrainerWorkload(request);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());
        TrainerWorkload actual = captor.getValue();
        assertThat(actual.getYears()).hasSize(1);
        assertThat(actual.getYears().get(0).getMonths()).hasSize(2);
        assertThat(getFirstMonth(actual).getMonth()).isEqualTo(TRAINING_MONTH.getValue());
        assertThat(getFirstMonth(actual).getTrainingSummaryDuration()).isEqualTo(60);
        assertThat(getSecondMonth(actual).getMonth()).isEqualTo(NEXT_MONTH.getValue());
        assertThat(getSecondMonth(actual).getTrainingSummaryDuration()).isEqualTo(30);
    }

    private TrainerWorkloadRequest buildRequest(ActionType actionType, int duration) {
        return new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .isActive(true)
                .trainingDate(LocalDate.of(YEAR, TRAINING_MONTH, 10))
                .trainingDuration(duration)
                .actionType(actionType);
    }

    private TrainerWorkload buildTrainerWorkload(int duration) {
        MonthSummary monthSummary = new MonthSummary(TRAINING_MONTH.getValue(), duration);
        YearSummary yearSummary = new YearSummary(YEAR, new ArrayList<>(List.of(monthSummary)));

        return new TrainerWorkload(USERNAME, FIRST_NAME, LAST_NAME, true, new ArrayList<>(List.of(yearSummary)));
    }

    private MonthSummary getFirstMonth(TrainerWorkload workload) {
        return workload.getYears().get(0).getMonths().get(0);
    }

    private MonthSummary getSecondMonth(TrainerWorkload workload) {
        return workload.getYears().get(0).getMonths().get(1);
    }
}
