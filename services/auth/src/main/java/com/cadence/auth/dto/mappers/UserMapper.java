package com.cadence.auth.dto.mappers;

import com.cadence.auth.domain.Role;
import com.cadence.auth.domain.User;
import com.cadence.auth.dto.request.RegisterRequest;
import com.cadence.auth.dto.response.UserResponse;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class UserMapper {

    public User mapRegisterRequestToUser(RegisterRequest request, String passwordHash, Role role) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordHash)
                .role(role)
                .build();
    }

    public UserResponse mapUserToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt());
    }
}
