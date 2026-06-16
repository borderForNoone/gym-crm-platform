package com.gym.crm.workload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.workload.service.TrainerWorkloadServiceImpl;
import gym.crm.platform.workload.openapi.ActionType;
import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerWorkloadController.class)
public class TrainerWorkloadControllerTest {
    private static final String BASE_URL = "/api/v1/trainer-workloads";
    private static final String USERNAME = "billy.herrington";
    private static final String FIRST_NAME = "Billy";
    private static final String LAST_NAME = "Herrington";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DURATION = 60;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private TrainerWorkloadServiceImpl trainerWorkloadService;

    @Test
    void updateTrainerWorkload_shouldReturnOk() throws Exception {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .isActive(true)
                .trainingDate(LocalDate.of(YEAR, MONTH, 10))
                .trainingDuration(DURATION)
                .actionType(ActionType.ADD);

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerWorkloadService).updateTrainerWorkload(request);
    }

    @Test
    void getTrainerMonthlyWorkload_shouldReturnOk() throws Exception {
        when(trainerWorkloadService.getMonthlyWorkload(USERNAME, YEAR, MONTH)).thenReturn(DURATION);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME)
                        .param("year", String.valueOf(YEAR))
                        .param("month", String.valueOf(MONTH)))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(DURATION)));

        verify(trainerWorkloadService).getMonthlyWorkload(USERNAME, YEAR, MONTH);
    }
}
