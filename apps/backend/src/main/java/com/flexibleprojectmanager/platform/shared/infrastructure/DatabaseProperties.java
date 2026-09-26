package com.flexibleprojectmanager.platform.shared.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.database")
public record DatabaseProperties(String path) {
}
