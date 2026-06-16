package com.cadence.auth.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.cadence.auth.dto.request.LoginRequest;
import com.cadence.auth.dto.request.RefreshRequest;
import com.cadence.auth.dto.request.RegisterRequest;
import com.cadence.auth.dto.response.ApiResult;
import com.cadence.auth.dto.response.AuthResponse;
import com.cadence.auth.dto.response.UserResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // Java's HttpURLConnection throws HttpRetryException on 401 in streaming mode.
        // Apache HttpClient handles 401 responses without retrying.
        restTemplate.getRestTemplate().setRequestFactory(
                new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()));
    }

    // ── register + login ─────────────────────────────────────────────────────

    @Test
    void registerThenLoginReturnsTokens() {
        RegisterRequest registerRequest = new RegisterRequest("alice", "alice@example.com", "password123");

        ResponseEntity<ApiResult<UserResponse>> registerResponse = restTemplate.exchange(
                "/auth/register", HttpMethod.POST, new HttpEntity<>(registerRequest),
                new ParameterizedTypeReference<ApiResult<UserResponse>>() {});

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody().getMessage()).isEqualTo("User registered successfully");
        assertThat(registerResponse.getBody().getData().username()).isEqualTo("alice");

        LoginRequest loginRequest = new LoginRequest("alice", "password123");

        ResponseEntity<ApiResult<AuthResponse>> loginResponse = restTemplate.exchange(
                "/auth/login", HttpMethod.POST, new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResult<AuthResponse>>() {});

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody().getMessage()).isEqualTo("Login successful");
        assertThat(loginResponse.getBody().getData().token()).isNotBlank();
        assertThat(loginResponse.getBody().getData().refreshToken()).isNotBlank();
        assertThat(loginResponse.getBody().getData().username()).isEqualTo("alice");
    }

    @Test
    void registerWithDuplicateUsernameReturnsConflict() {
        RegisterRequest registerRequest = new RegisterRequest("bob", "bob@example.com", "password123");
        restTemplate.exchange("/auth/register", HttpMethod.POST, new HttpEntity<>(registerRequest),
                new ParameterizedTypeReference<ApiResult<UserResponse>>() {});

        RegisterRequest duplicateRequest = new RegisterRequest("bob", "bob2@example.com", "password123");
        ResponseEntity<ApiResult<Void>> response = restTemplate.exchange(
                "/auth/register", HttpMethod.POST, new HttpEntity<>(duplicateRequest),
                new ParameterizedTypeReference<ApiResult<Void>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("bob");
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() {
        RegisterRequest registerRequest = new RegisterRequest("carol", "carol@example.com", "password123");
        restTemplate.exchange("/auth/register", HttpMethod.POST, new HttpEntity<>(registerRequest),
                new ParameterizedTypeReference<ApiResult<UserResponse>>() {});

        LoginRequest loginRequest = new LoginRequest("carol", "wrong-password");
        ResponseEntity<ApiResult<Void>> response = restTemplate.exchange(
                "/auth/login", HttpMethod.POST, new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResult<Void>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid username or password");
    }

    // ── refresh token ─────────────────────────────────────────────────────────

    @Test
    void refreshReturnsNewTokensAndRotates() {
        restTemplate.exchange("/auth/register", HttpMethod.POST,
                new HttpEntity<>(new RegisterRequest("dave", "dave@example.com", "password123")),
                new ParameterizedTypeReference<ApiResult<UserResponse>>() {});

        ResponseEntity<ApiResult<AuthResponse>> loginResponse = restTemplate.exchange(
                "/auth/login", HttpMethod.POST,
                new HttpEntity<>(new LoginRequest("dave", "password123")),
                new ParameterizedTypeReference<ApiResult<AuthResponse>>() {});

        String oldRefresh = loginResponse.getBody().getData().refreshToken();
        String oldAccess = loginResponse.getBody().getData().token();

        ResponseEntity<ApiResult<AuthResponse>> refreshResponse = restTemplate.exchange(
                "/auth/refresh", HttpMethod.POST,
                new HttpEntity<>(new RefreshRequest(oldRefresh)),
                new ParameterizedTypeReference<ApiResult<AuthResponse>>() {});

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResponse.getBody().getMessage()).isEqualTo("Token refreshed");
        assertThat(refreshResponse.getBody().getData().token()).isNotBlank().isNotEqualTo(oldAccess);
        assertThat(refreshResponse.getBody().getData().refreshToken()).isNotBlank().isNotEqualTo(oldRefresh);

        // old refresh must no longer work after rotation
        ResponseEntity<ApiResult<Void>> reuse = restTemplate.exchange(
                "/auth/refresh", HttpMethod.POST,
                new HttpEntity<>(new RefreshRequest(oldRefresh)),
                new ParameterizedTypeReference<ApiResult<Void>>() {});

        assertThat(reuse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refreshWithInvalidTokenReturnsUnauthorized() {
        ResponseEntity<ApiResult<Void>> response = restTemplate.exchange(
                "/auth/refresh", HttpMethod.POST,
                new HttpEntity<>(new RefreshRequest("not-a-valid-token")),
                new ParameterizedTypeReference<ApiResult<Void>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Refresh token not found");
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    void logoutInvalidatesRefreshToken() {
        restTemplate.exchange("/auth/register", HttpMethod.POST,
                new HttpEntity<>(new RegisterRequest("eve", "eve@example.com", "password123")),
                new ParameterizedTypeReference<ApiResult<UserResponse>>() {});

        ResponseEntity<ApiResult<AuthResponse>> loginResponse = restTemplate.exchange(
                "/auth/login", HttpMethod.POST,
                new HttpEntity<>(new LoginRequest("eve", "password123")),
                new ParameterizedTypeReference<ApiResult<AuthResponse>>() {});

        String refreshToken = loginResponse.getBody().getData().refreshToken();

        ResponseEntity<ApiResult<Void>> logoutResponse = restTemplate.exchange(
                "/auth/logout", HttpMethod.POST,
                new HttpEntity<>(new RefreshRequest(refreshToken)),
                new ParameterizedTypeReference<ApiResult<Void>>() {});

        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(logoutResponse.getBody().getMessage()).isEqualTo("Logged out successfully");

        // refresh after logout must fail
        ResponseEntity<ApiResult<Void>> refreshAfterLogout = restTemplate.exchange(
                "/auth/refresh", HttpMethod.POST,
                new HttpEntity<>(new RefreshRequest(refreshToken)),
                new ParameterizedTypeReference<ApiResult<Void>>() {});

        assertThat(refreshAfterLogout.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
