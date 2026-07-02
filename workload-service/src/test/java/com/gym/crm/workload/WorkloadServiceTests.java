package com.gym.crm.workload;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class WorkloadServiceTests {
    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void mainRuns() {
        assertDoesNotThrow(() -> WorkloadService.main(new String[]{}));
    }
}
