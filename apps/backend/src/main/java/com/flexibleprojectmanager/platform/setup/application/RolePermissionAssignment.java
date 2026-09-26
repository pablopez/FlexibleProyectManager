package com.flexibleprojectmanager.platform.setup.application;

import java.util.List;

public record RolePermissionAssignment(String role, List<String> permissions) {
}
