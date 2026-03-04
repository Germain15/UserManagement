/**
 * Endpoint de santé exposé sur GET /health.
 * Vérifie la connectivité à la base via un SELECT 1 réel pas une réponse statique.
 * Retourne 200 si tout va bien, 503 si la base est inaccessible.
 */
package com.imediaconsulting.userManagement.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.version:1.0.0}")
    private String version;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "timestamp", Instant.now().toString(),
                    "version", version,
                    "db_status", "connected"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "degraded",
                            "timestamp", Instant.now().toString(),
                            "version", version,
                            "db_status", "disconnected"
                    ));
        }
    }
}
