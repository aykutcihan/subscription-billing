package com.cadence.auth.dto.response;

import com.cadence.auth.domain.Role;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        Role role) {

    public AuthResponse(String token, String username, Role role) {
        this(token, "Bearer", username, role);
    }
}
