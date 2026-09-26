package com.flexibleprojectmanager.platform.setup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.flexibleprojectmanager.platform.setup.application.SetupData;
import com.flexibleprojectmanager.platform.setup.application.RolePermissionAssignment;
import com.flexibleprojectmanager.platform.setup.infrastructure.JdbcSetupRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
class SetupIntegrationTest {
    private static final Path databasePath = Path.of(
            System.getProperty("java.io.tmpdir"), "fpm-slice-1-" + UUID.randomUUID(), "test.sqlite");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcSetupRepository setupRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("app.database.path", () -> databasePath.toString());
    }

    @Test
    @Order(1)
    void freshDatabaseIsUninitialized() throws Exception {
        mockMvc.perform(get("/api/v1/setup/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initialized").value(false));
    }

    @Test
    @Order(2)
    void rejectsInvalidSetupInput() throws Exception {
        mockMvc.perform(post("/api/v1/setup/initialize")
                        .contentType("application/json")
                        .content("{\"organization\":{\"name\":\" \"},\"installation\":{\"name\":\"Main\"},\"administrator\":{\"email\":\"bad\",\"displayName\":\"Admin\",\"password\":\"short\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM system_initialization", Integer.class)).isZero();
    }

    @Test
    @Order(3)
    void failedBootstrapRollsBackTheInitializationMarker() {
        SetupData invalidData = new SetupData(
                UUID.randomUUID(), null, Instant.now(), UUID.randomUUID(), "Installation", "OTHER", "0.1.0",
                Instant.now(), UUID.randomUUID(), "admin@example.com", "Admin", "hash", Instant.now(),
                UUID.randomUUID(), Instant.now(), List.of("ADMIN", "USER", "VIEWER"), List.of("users:read"),
                List.of(new RolePermissionAssignment("ADMIN", List.of("users:read"))), Instant.now());

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> setupRepository.persist(invalidData)))
                .isInstanceOf(RuntimeException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM system_initialization", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM organizations", Integer.class)).isZero();
    }

    @Test
    @Order(4)
    void initializesAllBootstrapResourcesAndHashesPassword() throws Exception {
        mockMvc.perform(post("/api/v1/setup/initialize")
                        .contentType("application/json")
                        .content("{\"organization\":{\"name\":\" Example Studio \"},\"installation\":{\"name\":\"Main Workstation\"},\"administrator\":{\"email\":\"ADMIN@Example.com\",\"displayName\":\"Administrator\",\"password\":\"password123\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.initialized").value(true))
                .andExpect(jsonPath("$.organization.name").value("Example Studio"))
                .andExpect(jsonPath("$.installation.status").value("UNLICENSED"))
                .andExpect(jsonPath("$.administrator.email").value("admin@example.com"))
                .andExpect(content().string(not(containsString("password"))))
                .andExpect(content().string(not(containsString("passwordHash"))));

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM organizations", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM installations", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM organization_members", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM roles", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM permissions", Integer.class)).isEqualTo(12);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM member_roles", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM system_initialization", Integer.class)).isEqualTo(1);

        assertRolePermissions("ADMIN", "users:read", "users:create", "users:update", "projects:read",
                "projects:create", "projects:update", "projects:archive", "organization:read",
                "organization:update", "license:read", "license:manage", "audit:read");
        assertRolePermissions("USER", "projects:read", "projects:create", "projects:update", "organization:read");
        assertRolePermissions("VIEWER", "projects:read", "organization:read");

        String storedHash = jdbcTemplate.queryForObject("SELECT password_hash FROM users", String.class);
        assertThat(storedHash).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", storedHash)).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT email FROM users", String.class)).isEqualTo("admin@example.com");
    }

    private void assertRolePermissions(String role, String... expectedPermissions) {
        var actualPermissions = jdbcTemplate.queryForList(
                "SELECT p.code FROM permissions p JOIN role_permissions rp ON rp.permission_id = p.id "
                        + "JOIN roles r ON r.id = rp.role_id WHERE r.code = ? ORDER BY p.code",
                String.class, role);
        assertThat(actualPermissions).containsExactlyInAnyOrder(expectedPermissions);
    }

    @Test
    @Order(5)
    void reportsInitializedAndRejectsSecondAttempt() throws Exception {
        mockMvc.perform(get("/api/v1/setup/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initialized").value(true));

        mockMvc.perform(post("/api/v1/setup/initialize")
                        .contentType("application/json")
                        .content("{\"organization\":{\"name\":\"Other\"},\"installation\":{\"name\":\"Other\"},\"administrator\":{\"email\":\"other@example.com\",\"displayName\":\"Other\",\"password\":\"password123\"}}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SYSTEM_ALREADY_INITIALIZED"));

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM organizations", Integer.class)).isEqualTo(1);
    }
}
