package com.gym.crm.core.mapper;

import com.gym.crm.core.facade.dto.TrainingRequestDTO;
import com.gym.crm.core.facade.dto.TrainingResponseDTO;
import com.gym.crm.core.facade.dto.TrainingTypeDTO;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {
    @Mapping(target = "trainee", ignore = true)
    @Mapping(target = "trainer", ignore = true)
    @Mapping(target = "trainingType", ignore = true)
    Training toEntity(TrainingRequestDTO dto);

    @Mapping(source = "trainee.id", target = "traineeId")
    @Mapping(source = "trainer.id", target = "trainerId")
    @Mapping(source = "trainingType.trainingTypeName", target = "trainingTypeName")
    TrainingResponseDTO toDto(Training training);

    TrainingTypeDTO toDto(TrainingType trainingType);
}
