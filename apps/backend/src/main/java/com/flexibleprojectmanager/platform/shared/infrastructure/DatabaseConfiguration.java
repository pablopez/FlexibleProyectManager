package com.flexibleprojectmanager.platform.shared.infrastructure;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.sqlite.SQLiteDataSource;

@Configuration
@Profile("local")
@EnableConfigurationProperties(DatabaseProperties.class)
public class DatabaseConfiguration {

    @Bean
    DataSource dataSource(DatabaseProperties properties) {
        try {
            Path databasePath = Path.of(properties.path()).toAbsolutePath();
            if (databasePath.getParent() != null) {
                Files.createDirectories(databasePath.getParent());
            }

            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl("jdbc:sqlite:" + databasePath
                    + "?foreign_keys=on&journal_mode=WAL&busy_timeout=5000");
            return dataSource;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to prepare the SQLite database directory", exception);
        }
    }
}
