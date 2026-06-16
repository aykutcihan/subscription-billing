package com.cadence.auth.service;

import com.cadence.auth.domain.RefreshToken;
import com.cadence.auth.domain.Role;
import com.cadence.auth.domain.User;
import com.cadence.auth.dto.mappers.UserMapper;
import com.cadence.auth.dto.request.LoginRequest;
import com.cadence.auth.dto.request.RegisterRequest;
import com.cadence.auth.dto.response.AuthResponse;
import com.cadence.auth.dto.response.UserResponse;
import com.cadence.auth.exception.EmailAlreadyExistsException;
import com.cadence.auth.exception.UsernameAlreadyExistsException;
import com.cadence.auth.repository.UserRepository;
import com.cadence.auth.security.jwt.JwtUtils;
import com.cadence.auth.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = userMapper.mapRegisterRequestToUser(
                request, passwordEncoder.encode(request.password()), Role.MEMBER);

        return userMapper.mapUserToUserResponse(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String accessToken = jwtUtils.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.create(userDetails.getId());

        return new AuthResponse(accessToken, refreshToken.getToken(),
                userDetails.getUsername(), userDetails.getRole());
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken old = refreshTokenService.verifyValid(refreshTokenValue);
        User user = userRepository.findById(old.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for refresh token"));

        refreshTokenService.deleteByToken(refreshTokenValue);
        RefreshToken fresh = refreshTokenService.create(user.getId());
        String newAccess = jwtUtils.generateToken(UserDetailsImpl.fromUser(user));

        return new AuthResponse(newAccess, fresh.getToken(), user.getUsername(), user.getRole());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenService.deleteByToken(refreshTokenValue);
    }
}
