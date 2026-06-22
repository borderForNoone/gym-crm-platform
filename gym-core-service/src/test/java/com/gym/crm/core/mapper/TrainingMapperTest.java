package com.gym.crm.core.mapper;

import com.gym.crm.core.facade.dto.TrainingRequestDTO;
import com.gym.crm.core.facade.dto.TrainingResponseDTO;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.TrainingType;
import com.gym.crm.core.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TrainingMapperTest {
    private static final String TRAINING_NAME = "Morning Cardio";
    private static final String TRAINING_TYPE_NAME = "Cardio";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2026, 4, 4);
    private static final int TRAINING_DURATION = 60;
    private static final long VALID_ID = 1L;
    private static final long TRAINEE_ID = 1L;
    private static final long TRAINER_ID = 2L;

    private final TrainingMapper mapper = Mappers.getMapper(TrainingMapper.class);

    @Test
    void toEntity_shouldMapBasicFields_whenMapFromTrainingRequestDTO() {
        TrainingRequestDTO dto = buildTrainingRequestDTO();

        Training training = mapper.toEntity(dto);

        assertNotNull(training);
        assertEquals(dto.getTrainingName(), training.getTrainingName());
        assertEquals(dto.getTrainingDate(), training.getTrainingDate());
        assertEquals(dto.getTrainingDuration(), training.getTrainingDuration());
        assertNull(training.getTrainingType());
        assertNull(training.getTrainee());
        assertNull(training.getTrainer());
    }

    @Test
    void toDto_shouldMapAllFields_whenMapFromTrainingEntity() {
        Training training = buildTraining();

        TrainingResponseDTO responseDTO = mapper.toDto(training);

        assertNotNull(responseDTO);
        assertEquals(TRAINEE_ID, responseDTO.getTraineeId());
        assertEquals(TRAINER_ID, responseDTO.getTrainerId());
        assertEquals(TRAINING_NAME, responseDTO.getTrainingName());
        assertEquals(TRAINING_TYPE_NAME, responseDTO.getTrainingTypeName());
        assertEquals(TRAINING_DATE, responseDTO.getTrainingDate());
        assertEquals(TRAINING_DURATION, responseDTO.getTrainingDuration());
    }

    private TrainingRequestDTO buildTrainingRequestDTO() {
        TrainingRequestDTO dto = new TrainingRequestDTO();
        dto.setTraineeUsername("tom.tomas");
        dto.setTrainerUsername("julia.tomas");
        dto.setTrainingName(TRAINING_NAME);
        dto.setTrainingDate(TRAINING_DATE);
        dto.setTrainingDuration(TRAINING_DURATION);
        return dto;
    }

    private Training buildTraining() {
        User traineeUser = User.builder()
                .id(TRAINEE_ID)
                .firstName("Tom")
                .lastName("Tomas")
                .username("tom.tomas")
                .password("password")
                .isActive(true)
                .build();
        User trainerUser = User.builder()
                .id(TRAINER_ID)
                .firstName("Julia")
                .lastName("Tomas")
                .username("julia.tomas")
                .password("password")
                .isActive(true)
                .build();
        Trainee trainee = Trainee.builder()
                .id(TRAINEE_ID)
                .user(traineeUser)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("10 Sheep St")
                .build();
        Trainer trainer = Trainer.builder().id(TRAINER_ID).user(trainerUser).specialization(TrainingType.builder().trainingTypeName(TRAINING_TYPE_NAME).build())
                .build();

        return Training.builder()
                .id(VALID_ID)
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(TRAINING_NAME)
                .trainingType(TrainingType.builder().trainingTypeName(TRAINING_TYPE_NAME).build())
                .trainingDate(TRAINING_DATE)
                .trainingDuration(TRAINING_DURATION)
                .build();
    }
}