package com.flexibleprojectmanager.platform.setup.infrastructure;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.flexibleprojectmanager.platform.setup.application.SetupData;
import com.flexibleprojectmanager.platform.setup.application.SetupRepository;
import com.flexibleprojectmanager.platform.setup.application.SystemAlreadyInitializedException;

@Repository
public class JdbcSetupRepository implements SetupRepository {
    private final JdbcTemplate jdbc;

    public JdbcSetupRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean isInitialized() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM system_initialization WHERE singleton_id = 1", Integer.class);
        return count != null && count > 0;
    }

    @Override
    public void persist(SetupData data) {
        int inserted = jdbc.update("INSERT OR IGNORE INTO system_initialization (singleton_id, initialized_at) VALUES (1, ?)",
                data.initializedAt().toString());
        if (inserted != 1) {
            throw new SystemAlreadyInitializedException();
        }

        jdbc.update("INSERT INTO organizations (id, name, status, created_at, updated_at) VALUES (?, ?, 'ACTIVE', ?, ?)",
                data.organizationId().toString(), data.organizationName(), data.organizationCreatedAt().toString(),
                data.organizationCreatedAt().toString());
        jdbc.update("INSERT INTO installations (id, organization_id, name, platform, application_version, status, created_at) VALUES (?, ?, ?, ?, ?, 'UNLICENSED', ?)",
                data.installationId().toString(), data.organizationId().toString(), data.installationName(), data.platform(),
                data.applicationVersion(), data.installationCreatedAt().toString());
        jdbc.update("INSERT INTO users (id, email, password_hash, display_name, status, created_at, updated_at) VALUES (?, ?, ?, ?, 'ACTIVE', ?, ?)",
                data.userId().toString(), data.email(), data.passwordHash(), data.displayName(), data.userCreatedAt().toString(),
                data.userCreatedAt().toString());
        jdbc.update("INSERT INTO organization_members (id, user_id, organization_id, status, joined_at) VALUES (?, ?, ?, 'ACTIVE', ?)",
                data.memberId().toString(), data.userId().toString(), data.organizationId().toString(), data.memberJoinedAt().toString());

        Map<String, String> roleIds = new HashMap<>();
        for (String role : data.roles()) {
            String id = java.util.UUID.randomUUID().toString();
            roleIds.put(role, id);
            jdbc.update("INSERT INTO roles (id, code, system_defined) VALUES (?, ?, 1)", id, role);
        }
        Map<String, String> permissionIds = new HashMap<>();
        for (String permission : data.permissions()) {
            String id = java.util.UUID.randomUUID().toString();
            permissionIds.put(permission, id);
            jdbc.update("INSERT INTO permissions (id, code) VALUES (?, ?)", id, permission);
        }
        for (var assignment : data.rolePermissions()) {
            for (String permission : assignment.permissions()) {
                jdbc.update("INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)",
                        roleIds.get(assignment.role()), permissionIds.get(permission));
            }
        }
        jdbc.update("INSERT INTO member_roles (member_id, role_id) VALUES (?, ?)", data.memberId().toString(), roleIds.get("ADMIN"));
    }
}
