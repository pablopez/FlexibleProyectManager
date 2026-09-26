package com.flexibleprojectmanager.platform.system.api;

import java.time.Instant;

public record HealthResponse(
        HealthComponentStatus status,
        HealthComponentStatus backend,
        HealthComponentStatus database,
        Instant timestamp) {
}
