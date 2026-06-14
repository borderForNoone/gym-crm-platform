package com.gym.crm.platform.service.impl;

import com.gym.crm.platform.facade.dto.TrainingResponseDTO;
import com.gym.crm.platform.facade.dto.TrainingTypeDTO;
import com.gym.crm.platform.mapper.TrainingMapper;
import com.gym.crm.platform.model.Training;
import com.gym.crm.platform.repository.TrainingRepository;
import com.gym.crm.platform.repository.TrainingRepositoryCriteria;
import com.gym.crm.platform.repository.TrainingTypeRepository;
import com.gym.crm.platform.search.filter.TraineeTrainingFilter;
import com.gym.crm.platform.search.filter.TrainerTrainingFilter;
import com.gym.crm.platform.service.TrainingService;
import com.gym.crm.platform.service.common.UserInputValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {
    private final UserInputValidator validator;
    private final TrainingMapper mapper;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingRepositoryCriteria trainingRepositoryCriteria;

    @Transactional
    @Override
    public Training create(Training training) {
        log.info("Creating training: {}", training.getTrainingName());

        return trainingRepository.save(training);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrainingResponseDTO> getTraineeTrainings(@Valid TraineeTrainingFilter filter) {
        validator.validate(filter, "Filter");
        log.info("Getting trainee trainings by filter: {}", filter);

        return trainingRepositoryCriteria.findByTraineeCriteria(filter)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrainingResponseDTO> getTrainerTrainings(@Valid TrainerTrainingFilter filter) {
        validator.validate(filter, "Filter");
        log.info("Getting trainer trainings by filter: {}", filter);

        return trainingRepositoryCriteria.findByTrainerCriteria(filter)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrainingTypeDTO> getAllTrainingTypes() {
        return trainingTypeRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }
}