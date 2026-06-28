package com.gym.crm.workload.controller;

import com.gym.crm.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(TrainerWorkloadController.class)
class TrainerWorkloadControllerTest {
    private static final String BASE_URL = "/api/v1/trainer-workloads";
    private static final String USERNAME = "billy.herrington";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DURATION = 60;
    private static final int EXPECTED_HTTP_OK = 200;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainerWorkloadService trainerWorkloadService;

    @Test
    void getTrainerMonthlyWorkload_shouldReturnOk() throws Exception {
        when(trainerWorkloadService.getMonthlyWorkload(USERNAME, YEAR, MONTH)).thenReturn(DURATION);

        MvcResult result = mockMvc.perform(get(BASE_URL + "/" + USERNAME)
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