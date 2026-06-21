package com.cadence.auth.api;

import com.cadence.auth.dto.request.LoginRequest;
import com.cadence.auth.dto.request.RefreshRequest;
import com.cadence.auth.dto.request.RegisterRequest;
import com.cadence.auth.dto.response.ApiResult;
import com.cadence.auth.dto.response.AuthResponse;
import com.cadence.auth.dto.response.UserResponse;
import com.cadence.auth.service.AuthService;
import com.cadence.auth.util.Messages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResult<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(created, Messages.USER_REGISTERED));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResult<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(ApiResult.success(auth, Messages.LOGIN_SUCCESS));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse auth = authService.refreshAndIssue(request.refreshToken());
        return ResponseEntity.ok(ApiResult.success(auth, Messages.TOKEN_REFRESHED));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResult<Void>> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResult.<Void>builder().message(Messages.LOGOUT_SUCCESS).build());
    }
}
