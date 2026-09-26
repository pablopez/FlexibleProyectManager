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

class HealthServiceTest {

    @Test
    void reportsBackendUpAndDatabaseDownWhenProbeFails() {
        DatabaseHealthProbe probe = mock(DatabaseHealthProbe.class);
        when(probe.isAvailable()).thenReturn(false);
        Clock clock = Clock.fixed(Instant.parse("2026-09-26T11:00:00Z"), ZoneOffset.UTC);

        HealthService.HealthCheckResult result = new HealthService(probe, clock).check();

        assertThat(result.status().value()).isEqualTo(503);
        assertThat(result.response().status()).hasToString("DOWN");
        assertThat(result.response().backend()).hasToString("UP");
        assertThat(result.response().database()).hasToString("DOWN");
        assertThat(result.response().timestamp()).isEqualTo(Instant.parse("2026-09-26T11:00:00Z"));
    }
}
