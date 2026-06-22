package com.gym.crm.core.service.impl;

import com.gym.crm.core.client.workload.WorkloadRequestMapper;
import com.gym.crm.core.client.workload.WorkloadUpdateEvent;
import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import com.gym.crm.core.facade.dto.TrainingRequestDTO;
import com.gym.crm.core.facade.dto.TrainingResponseDTO;
import com.gym.crm.core.facade.dto.TrainingTypeDTO;
import com.gym.crm.core.mapper.TrainingMapper;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.TrainingType;
import com.gym.crm.core.model.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingRepository;
import com.gym.crm.core.repository.TrainingRepositoryCriteria;
import com.gym.crm.core.repository.TrainingTypeRepository;
import com.gym.crm.core.search.filter.TraineeTrainingFilter;
import com.gym.crm.core.search.filter.TrainerTrainingFilter;
import com.gym.crm.core.service.common.UserInputValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    private static final String TRAINEE_USERNAME = "Julia.Tomas";
    private static final String TRAINER_USERNAME = "Tom.Trainer";

    @Mock
    private UserInputValidator validator;
    @Mock
    private TrainingMapper mapper;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private TrainingRepositoryCriteria trainingRepositoryCriteria;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private WorkloadRequestMapper requestMapper;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private TrainingServiceImpl service;

    @Test
    void create_shouldSaveAndPublishEvent() {
        TrainingRequestDTO dto = new TrainingRequestDTO();
        dto.setTraineeUsername("Nora.Pemberton");
        dto.setTrainerUsername("Callum.Whitfield");
        dto.setTrainingName("Morning Cardio");
        dto.setTrainingDate(LocalDate.of(2026, Month.JUNE, 10));
        dto.setTrainingDuration(60);

        TrainingType type = TrainingType.builder().id(1L).trainingTypeName("Yoga").build();
        Trainee trainee = Trainee.builder()
                .id(1L)
                .user(User.builder().username("Nora.Pemberton").build())
                .build();
        Trainer trainer = Trainer.builder()
                .id(2L)
                .user(User.builder().username("Callum.Whitfield").build())
                .specialization(type)
                .build();
        TrainerWorkloadRequest workloadRequest = new TrainerWorkloadRequest();

        when(traineeRepository.findByUser_Username("Nora.Pemberton")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUser_Username("Callum.Whitfield")).thenReturn(Optional.of(trainer));
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requestMapper.toRequest(any(Training.class), eq(ActionType.ADD))).thenReturn(workloadRequest);

        Training result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getTrainingName()).isEqualTo("Morning Cardio");
        assertThat(result.getTrainingType()).isEqualTo(type);
        verify(trainingRepository).save(any(Training.class));
        verify(publisher).publishEvent(any(WorkloadUpdateEvent.class));
        verify(requestMapper).toRequest(any(Training.class), eq(ActionType.ADD));
    }

    @Test
    void getTraineeTrainings_shouldReturnMappedList() {
        TraineeTrainingFilter filter = buildTraineeFilter();
        Training training = buildTraining();
        TrainingResponseDTO dto = buildTrainingResponseDTO();

        when(trainingRepositoryCriteria.findByTraineeCriteria(filter)).thenReturn(List.of(training));
        when(mapper.toDto(training)).thenReturn(dto);

        List<TrainingResponseDTO> result = service.getTraineeTrainings(filter);

        assertThat(result).hasSize(1).contains(dto);
        verify(trainingRepositoryCriteria).findByTraineeCriteria(filter);
        verify(mapper).toDto(training);
    }

    @Test
    void getTraineeTrainings_shouldReturnEmptyList_whenNoTrainings() {
        TraineeTrainingFilter filter = buildTraineeFilter();

        when(trainingRepositoryCriteria.findByTraineeCriteria(filter)).thenReturn(List.of());

        assertThat(service.getTraineeTrainings(filter)).isEmpty();
        verify(trainingRepositoryCriteria).findByTraineeCriteria(filter);
    }

    @Test
    void getTrainerTrainings_shouldReturnMappedList() {
        TrainerTrainingFilter filter = buildTrainerFilter();
        Training training = buildTraining();
        TrainingResponseDTO dto = buildTrainingResponseDTO();

        when(trainingRepositoryCriteria.findByTrainerCriteria(filter)).thenReturn(List.of(training));
        when(mapper.toDto(training)).thenReturn(dto);

        List<TrainingResponseDTO> result = service.getTrainerTrainings(filter);

        assertThat(result).hasSize(1).contains(dto);
        verify(trainingRepositoryCriteria).findByTrainerCriteria(filter);
        verify(mapper).toDto(training);
    }

    @Test
    void getTrainerTrainings_shouldReturnEmptyList_whenNoTrainings() {
        TrainerTrainingFilter filter = buildTrainerFilter();

        when(trainingRepositoryCriteria.findByTrainerCriteria(filter)).thenReturn(List.of());

        assertThat(service.getTrainerTrainings(filter)).isEmpty();
        verify(trainingRepositoryCriteria).findByTrainerCriteria(filter);
    }

    @Test
    void getAllTrainingTypes_shouldReturnMappedList() {
        TrainingType type = TrainingType.builder().trainingTypeName("Cardio").build();
        TrainingTypeDTO dto = TrainingTypeDTO.builder().trainingTypeName("Cardio").build();

        when(trainingTypeRepository.findAll()).thenReturn(List.of(type));
        when(mapper.toDto(type)).thenReturn(dto);

        List<TrainingTypeDTO> result = service.getAllTrainingTypes();

        assertThat(result).hasSize(1).contains(dto);
        verify(trainingTypeRepository).findAll();
        verify(mapper).toDto(type);
    }

    @Test
    void getAllTrainingTypes_shouldReturnEmptyList_whenNoTypes() {
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        assertThat(service.getAllTrainingTypes()).isEmpty();
    }

    private Training buildTraining() {
        Trainee trainee = Trainee.builder().id(1L).build();
        Trainer trainer = Trainer.builder().id(1L).build();
        TrainingType type = TrainingType.builder().id(1L).trainingTypeName("Cardio").build();

        return Training.builder()
                .trainingName("Morning Cardio")
                .trainingDate(LocalDate.of(2024, Month.MARCH, 10))
                .trainingDuration(60)
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(type)
                .build();
    }

    private TrainingResponseDTO buildTrainingResponseDTO() {
        return TrainingResponseDTO.builder()
                .trainingName("Morning Cardio")
                .trainingDate(LocalDate.of(2024, 3, 10))
                .trainingTypeName("Cardio")
                .trainingDuration(60).build();
    }

    private TraineeTrainingFilter buildTraineeFilter() {
        return TraineeTrainingFilter.builder().username(TRAINEE_USERNAME).fromDate(LocalDate.of(2024, 1, 1)).toDate(LocalDate.of(2024, 12, 31)).build();
    }

    private TrainerTrainingFilter buildTrainerFilter() {
        return TrainerTrainingFilter.builder().username(TRAINER_USERNAME).fromDate(LocalDate.of(2024, 1, 1)).toDate(LocalDate.of(2024, 12, 31)).build();
    }
}