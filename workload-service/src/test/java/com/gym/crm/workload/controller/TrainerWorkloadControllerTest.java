package com.gym.crm.workload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.workload.config.SecurityConfig;
import com.gym.crm.workload.security.ServiceAuthenticationFilter;
import com.gym.crm.workload.security.ServiceJwtValidator;
import com.gym.crm.workload.service.TrainerWorkloadServiceImpl;
import gym.crm.platform.workload.openapi.ActionType;
import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerWorkloadController.class)
@Import({SecurityConfig.class, ServiceAuthenticationFilter.class, ServiceJwtValidator.class})
class TrainerWorkloadControllerTest {
    private static final String BASE_URL = "/api/v1/trainer-workloads";
    private static final String USERNAME = "billy.herrington";
    private static final String FIRST_NAME = "Billy";
    private static final String LAST_NAME = "Herrington";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DURATION = 60;
    private static final String VALID_TOKEN = "valid-service-token";
    private static final String SERVICE_SUBJECT = "gym-core-service";
    private static final int EXPECTED_HTTP_OK = 200;
    private static final int EXPECTED_HTTP_FORBIDDEN = 403;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private ServiceJwtValidator serviceJwtValidator;
    @MockitoBean
    private TrainerWorkloadServiceImpl trainerWorkloadService;

    @Test
    void updateTrainerWorkload_shouldReturnOk_whenServiceTokenIsValid() throws Exception {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .isActive(true)
                .trainingDate(LocalDate.of(YEAR, MONTH, 10))
                .trainingDuration(DURATION)
                .actionType(ActionType.ADD);

        when(serviceJwtValidator.isValidServiceToken(VALID_TOKEN)).thenReturn(true);
        when(serviceJwtValidator.extractSubject(VALID_TOKEN)).thenReturn(SERVICE_SUBJECT);

        MvcResult result = mockMvc.perform(put(BASE_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andReturn();

        int actualStatus = result.getResponse().getStatus();
        assertThat(actualStatus).isEqualTo(EXPECTED_HTTP_OK);
        verify(trainerWorkloadService).updateTrainerWorkload(request);
    }

    @Test
    void updateTrainerWorkload_shouldReturnForbidden_whenServiceTokenIsMissing() throws Exception {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .isActive(true)
                .trainingDate(LocalDate.of(YEAR, MONTH, 10))
                .trainingDuration(DURATION)
                .actionType(ActionType.ADD);

        MvcResult result = mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andReturn();

        int actualStatus = result.getResponse().getStatus();
        assertThat(actualStatus).isEqualTo(EXPECTED_HTTP_FORBIDDEN);
    }

    @Test
    void getTrainerMonthlyWorkload_shouldReturnOk_whenServiceTokenIsValid() throws Exception {
        when(serviceJwtValidator.isValidServiceToken(VALID_TOKEN)).thenReturn(true);
        when(serviceJwtValidator.extractSubject(VALID_TOKEN)).thenReturn(SERVICE_SUBJECT);
        when(trainerWorkloadService.getMonthlyWorkload(USERNAME, YEAR, MONTH)).thenReturn(DURATION);

        MvcResult result = mockMvc.perform(get(BASE_URL + "/" + USERNAME)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                        .param("year", String.valueOf(YEAR))
                        .param("month", String.valueOf(MONTH)))
                .andReturn();

        int actualStatus = result.getResponse().getStatus();
        String actualBody = result.getResponse().getContentAsString();
        assertThat(actualStatus).isEqualTo(EXPECTED_HTTP_OK);
        assertThat(actualBody).isEqualTo(String.valueOf(DURATION));
        verify(trainerWorkloadService).getMonthlyWorkload(USERNAME, YEAR, MONTH);
    }
}