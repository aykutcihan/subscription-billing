package com.cadence.auth.dto.response;

import com.cadence.auth.domain.Role;

public record AuthResponse(
        String token,
        String refreshToken,
        String tokenType,
        String username,
        Role role) {

    public AuthResponse(String token, String refreshToken, String username, Role role) {
        this(token, refreshToken, "Bearer", username, role);
    }
}
