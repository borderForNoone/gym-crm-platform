package com.gym.crm.workload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.workload.config.NoSecurityConfig;
import com.gym.crm.workload.security.JwtService;
import com.gym.crm.workload.security.imp.RedisTokenBlacklistService;
import com.gym.crm.workload.service.impl.TrainerWorkloadServiceImpl;
import gym.crm.platform.workload.openapi.ActionType;
import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerWorkloadController.class)
@Import(NoSecurityConfig.class)
class TrainerWorkloadControllerTest {
    private static final String BASE_URL = "/api/v1/trainer-workloads";
    private static final String USERNAME = "billy.herrington";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DURATION = 60;
    private static final int EXPECTED_HTTP_OK = 200;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainerWorkloadServiceImpl trainerWorkloadService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private RedisTokenBlacklistService redisTokenBlacklistService;

    @Test
    void updateTrainerWorkload_shouldReturnOk() throws Exception {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .trainerFirstName("Billy")
                .trainerLastName("Herrington")
                .isActive(true)
                .trainingDate(LocalDate.of(YEAR, MONTH, 10))
                .trainingDuration(DURATION)
                .actionType(ActionType.ADD);

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerWorkloadService).updateTrainerWorkload(any(TrainerWorkloadRequest.class));
    }

    @Test
    void getTrainerMonthlyWorkload_shouldReturnOk() throws Exception {
        when(trainerWorkloadService.getMonthlyWorkload(USERNAME, YEAR, MONTH)).thenReturn(DURATION);

        MvcResult result = mockMvc.perform(get(BASE_URL + "/" + USERNAME)
                        .param("year", String.valueOf(YEAR))
                        .param("month", String.valueOf(MONTH)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(EXPECTED_HTTP_OK);
        assertThat(result.getResponse().getContentAsString()).isEqualTo(String.valueOf(DURATION));
        verify(trainerWorkloadService).getMonthlyWorkload(USERNAME, YEAR, MONTH);
    }
}