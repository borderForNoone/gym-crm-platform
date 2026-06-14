package com.gym.crm.discovery.mapper;

import com.gym.crm.discovery.facade.dto.TraineeInfoDTO;
import com.gym.crm.discovery.facade.dto.TraineeRequestDTO;
import com.gym.crm.discovery.facade.dto.TraineeResponseDTO;
import com.gym.crm.discovery.facade.dto.TraineeUpdateDTO;
import org.gym.crm.rest.TraineeCreateRequest;
import org.gym.crm.rest.TraineeCreateResponse;
import org.gym.crm.rest.TraineeGetResponse;
import org.gym.crm.rest.TraineeUpdateRequest;
import org.gym.crm.rest.TraineeUpdateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TraineeRestMapper {
    TraineeRequestDTO toDto(TraineeCreateRequest request);

    TraineeCreateResponse toRest(TraineeResponseDTO dto);

    TraineeGetResponse toRest(TraineeInfoDTO dto);

    @Mapping(target = "username", source = "username")
    TraineeUpdateDTO toDto(String username, TraineeUpdateRequest request);

    TraineeUpdateResponse toRestUpdateResponse(TraineeResponseDTO dto);
}
