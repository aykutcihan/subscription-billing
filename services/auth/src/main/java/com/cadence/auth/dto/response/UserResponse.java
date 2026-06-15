package com.cadence.auth.dto.response;

import com.cadence.auth.domain.Role;
import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        Instant createdAt) {
}
