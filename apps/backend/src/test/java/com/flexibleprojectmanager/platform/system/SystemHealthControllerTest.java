package com.flexibleprojectmanager.platform.system;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.flexibleprojectmanager.platform.system.api.SystemHealthController;
import com.flexibleprojectmanager.platform.system.application.HealthService;
import com.flexibleprojectmanager.platform.system.application.HealthService.HealthCheckResult;
import com.flexibleprojectmanager.platform.system.application.HealthService.HealthStatus;

class SystemHealthControllerTest {

    @Test
    void mapsUnavailableDatabaseTo503PublicContract() throws Exception {
        HealthService healthService = mock(HealthService.class);
        when(healthService.check()).thenReturn(new HealthCheckResult(
                HealthStatus.DOWN,
                HealthStatus.UP,
                HealthStatus.DOWN,
                Instant.parse("2026-09-26T11:00:00Z")));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new SystemHealthController(healthService))
                .build();

        mockMvc.perform(get("/api/v1/system/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.backend").value("UP"))
                .andExpect(jsonPath("$.database").value("DOWN"))
                .andExpect(jsonPath("$.timestamp").value("2026-09-26T11:00:00Z"));
    }
}
