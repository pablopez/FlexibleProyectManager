package com.flexibleprojectmanager.platform.system.application;

import java.time.Clock;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.flexibleprojectmanager.platform.system.api.HealthComponentStatus;
import com.flexibleprojectmanager.platform.system.api.HealthResponse;

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
        HealthComponentStatus databaseStatus = databaseAvailable
                ? HealthComponentStatus.UP
                : HealthComponentStatus.DOWN;
        HealthComponentStatus overallStatus = databaseAvailable
                ? HealthComponentStatus.UP
                : HealthComponentStatus.DOWN;

        HealthResponse response = new HealthResponse(
                overallStatus,
                HealthComponentStatus.UP,
                databaseStatus,
                Instant.now(clock));

        return new HealthCheckResult(response, databaseAvailable ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE);
    }

    public record HealthCheckResult(HealthResponse response, HttpStatus status) {
    }
}
