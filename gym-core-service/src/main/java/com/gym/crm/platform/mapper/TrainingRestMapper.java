package com.gym.crm.platform.mapper;

import com.gym.crm.platform.facade.dto.TrainingRequestDTO;
import com.gym.crm.platform.facade.dto.TrainingResponseDTO;
import com.gym.crm.platform.facade.dto.TrainingTypeDTO;
import org.gym.crm.rest.GetTraineeTrainingResponse;
import org.gym.crm.rest.GetTrainerTrainingResponse;
import org.gym.crm.rest.TrainingCreateRequest;
import org.gym.crm.rest.TrainingTypeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainingRestMapper {
    TrainingRequestDTO toDto(TrainingCreateRequest request);

    TrainingTypeResponse toRest(TrainingTypeDTO dto);

    GetTraineeTrainingResponse toRestTraineeResponse(TrainingResponseDTO dto);

    GetTrainerTrainingResponse toRestTrainerResponse(TrainingResponseDTO dto);
}
