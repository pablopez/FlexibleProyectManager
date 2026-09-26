package com.flexibleprojectmanager.platform.system.application;

import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private final DatabaseHealthProbe databaseHealthProbe;
    private final Clock clock;

    @Autowired
    public HealthService(DatabaseHealthProbe databaseHealthProbe) {
        this(databaseHealthProbe, Clock.systemUTC());
    }

    public HealthService(DatabaseHealthProbe databaseHealthProbe, Clock clock) {
        this.databaseHealthProbe = databaseHealthProbe;
        this.clock = clock;
    }

    public HealthCheckResult check() {
        boolean databaseAvailable = databaseHealthProbe.isAvailable();
        HealthStatus databaseStatus = databaseAvailable ? HealthStatus.UP : HealthStatus.DOWN;
        HealthStatus overallStatus = databaseAvailable ? HealthStatus.UP : HealthStatus.DOWN;

        return new HealthCheckResult(
                overallStatus,
                HealthStatus.UP,
                databaseStatus,
                Instant.now(clock));
    }

    public enum HealthStatus {
        UP,
        DOWN
    }

    public record HealthCheckResult(
            HealthStatus status,
            HealthStatus backend,
            HealthStatus database,
            Instant timestamp) {
    }
}
