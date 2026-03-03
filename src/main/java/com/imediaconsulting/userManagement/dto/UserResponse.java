package com.imediaconsulting.userManagement.dto;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        Instant createdAt
) {}
