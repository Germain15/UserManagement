package com.imediaconsulting.userManagement.config;


import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection ignored = dataSource.getConnection()) {
            return Health.up()
                    .withDetail("db_status", "connected")
                    .withDetail("version", "1.0.0")
                    .withDetail("timestamp", Instant.now())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("db_status", "disconnected")
                    .build();
        }
    }
}