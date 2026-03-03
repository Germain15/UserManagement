/**
 * Réponse retournée par l'API après création ou listage.
 * Contient uniquement les champs publics — aucune donnée sensible.
 */
package com.imediaconsulting.userManagement.dto;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        Instant createdAt
) {}
