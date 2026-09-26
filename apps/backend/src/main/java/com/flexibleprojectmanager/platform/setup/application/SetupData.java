package com.flexibleprojectmanager.platform.setup.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SetupData(
        UUID organizationId,
        String organizationName,
        Instant organizationCreatedAt,
        UUID installationId,
        String installationName,
        String platform,
        String applicationVersion,
        Instant installationCreatedAt,
        UUID userId,
        String email,
        String displayName,
        String passwordHash,
        Instant userCreatedAt,
        UUID memberId,
        Instant memberJoinedAt,
        List<String> roles,
        List<String> permissions,
        List<RolePermissionAssignment> rolePermissions,
        Instant initializedAt) {
}
