package com.gym.crm.core.service.impl;

import com.gym.crm.core.client.workload.WorkloadRequestMapper;
import com.gym.crm.core.client.workload.WorkloadUpdateEvent;
import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import com.gym.crm.core.facade.dto.TrainingResponseDTO;
import com.gym.crm.core.facade.dto.TrainingTypeDTO;
import com.gym.crm.core.mapper.TrainingMapper;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.repository.TrainingRepository;
import com.gym.crm.core.repository.TrainingRepositoryCriteria;
import com.gym.crm.core.repository.TrainingTypeRepository;
import com.gym.crm.core.search.filter.TraineeTrainingFilter;
import com.gym.crm.core.search.filter.TrainerTrainingFilter;
import com.gym.crm.core.service.TrainingService;
import com.gym.crm.core.service.common.UserInputValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final WorkloadRequestMapper requestMapper;
    private final ApplicationEventPublisher publisher;

    @Transactional
    @Override
    public Training create(Training training) {
        log.info("Creating training: {}", training.getTrainingName());
        Training created = trainingRepository.save(training);

        TrainerWorkloadRequest workloadRequest = requestMapper.toRequest(created, ActionType.ADD);
        publisher.publishEvent(new WorkloadUpdateEvent(List.of(workloadRequest)));

        log.info("Training created with id: {}", created.getId());
        return created;
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