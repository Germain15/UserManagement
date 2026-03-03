/**
 * Format uniforme pour toutes les réponses d'erreur de l'API (400, 409, 500...).
 * Permet au client de toujours savoir où trouver le message d'erreur.
 */
package com.imediaconsulting.userManagement.exception;

import java.time.Instant;

public record ApiError(
        String error,
        String message,
        Instant timestamp
) {
    public ApiError(String error, String message) {
        this(error, message, Instant.now());
    }
}