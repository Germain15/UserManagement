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