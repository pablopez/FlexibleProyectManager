package com.flexibleprojectmanager.platform.system.infrastructure;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.flexibleprojectmanager.platform.system.application.DatabaseHealthProbe;

@Component
public class JdbcDatabaseHealthProbe implements DatabaseHealthProbe {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDatabaseHealthProbe(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isAvailable() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Integer.valueOf(1).equals(result);
        } catch (DataAccessException exception) {
            return false;
        }
    }
}
