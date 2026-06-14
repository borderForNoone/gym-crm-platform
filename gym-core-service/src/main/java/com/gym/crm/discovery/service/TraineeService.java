package com.gym.crm.discovery.service;

import com.gym.crm.discovery.facade.dto.CreatedTrainee;
import com.gym.crm.discovery.facade.dto.TraineeInfoDTO;
import com.gym.crm.discovery.facade.dto.TraineeResponseDTO;
import com.gym.crm.discovery.facade.dto.TraineeUpdateDTO;
import com.gym.crm.discovery.facade.dto.TrainerAssignmentUpdateDTO;
import com.gym.crm.discovery.facade.dto.TrainerInfoDTO;
import com.gym.crm.discovery.model.Trainee;
import com.gym.crm.discovery.model.Trainer;
import com.gym.crm.discovery.model.Training;
import com.gym.crm.discovery.search.filter.TraineeTrainingFilter;

import javax.naming.AuthenticationException;
import java.util.List;

public interface TraineeService {
    CreatedTrainee create(Trainee trainee);

    TraineeResponseDTO update(TraineeUpdateDTO trainee);

    TraineeInfoDTO getTraineeByUsername(String username);

    void changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException;

    Trainee setActive(String username, boolean active);

    void deleteByUsername(String username);

    List<Training> getTrainings(TraineeTrainingFilter filter);

    List<Trainer> getUnassignedTrainers(String traineeUsername);

    List<TrainerInfoDTO> updateTrainersList(TrainerAssignmentUpdateDTO dto);

    Trainee updateProfile(String username, Trainee updatedData);
}
