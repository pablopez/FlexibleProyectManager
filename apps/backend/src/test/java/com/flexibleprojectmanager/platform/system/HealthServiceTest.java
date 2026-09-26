package com.flexibleprojectmanager.platform.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.flexibleprojectmanager.platform.system.application.DatabaseHealthProbe;
import com.flexibleprojectmanager.platform.system.application.HealthService;
import com.flexibleprojectmanager.platform.system.application.HealthService.HealthStatus;

class HealthServiceTest {

    @Test
    void reportsBackendUpAndDatabaseDownWhenProbeFails() {
        DatabaseHealthProbe probe = mock(DatabaseHealthProbe.class);
        when(probe.isAvailable()).thenReturn(false);
        Clock clock = Clock.fixed(Instant.parse("2026-09-26T11:00:00Z"), ZoneOffset.UTC);

        HealthService.HealthCheckResult result = new HealthService(probe, clock).check();

        assertThat(result.status()).isEqualTo(HealthStatus.DOWN);
        assertThat(result.backend()).isEqualTo(HealthStatus.UP);
        assertThat(result.database()).isEqualTo(HealthStatus.DOWN);
        assertThat(result.timestamp()).isEqualTo(Instant.parse("2026-09-26T11:00:00Z"));
    }
}
