package com.cadence.auth.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.cadence.auth.dto.request.LoginRequest;
import com.cadence.auth.dto.request.RegisterRequest;
import com.cadence.auth.dto.response.AuthResponse;
import com.cadence.auth.dto.response.ErrorResponse;
import com.cadence.auth.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Test
    void registerThenLoginReturnsJwt() {
        RegisterRequest registerRequest = new RegisterRequest("alice", "alice@example.com", "password123");

        ResponseEntity<UserResponse> registerResponse =
                restTemplate.postForEntity("/auth/register", registerRequest, UserResponse.class);

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody()).isNotNull();
        assertThat(registerResponse.getBody().username()).isEqualTo("alice");

        LoginRequest loginRequest = new LoginRequest("alice", "password123");

        ResponseEntity<AuthResponse> loginResponse =
                restTemplate.postForEntity("/auth/login", loginRequest, AuthResponse.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().token()).isNotBlank();
        assertThat(loginResponse.getBody().username()).isEqualTo("alice");
    }

    @Test
    void registerWithDuplicateUsernameReturnsConflict() {
        RegisterRequest registerRequest = new RegisterRequest("bob", "bob@example.com", "password123");
        restTemplate.postForEntity("/auth/register", registerRequest, UserResponse.class);

        RegisterRequest duplicateRequest = new RegisterRequest("bob", "bob2@example.com", "password123");
        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity("/auth/register", duplicateRequest, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() {
        RegisterRequest registerRequest = new RegisterRequest("carol", "carol@example.com", "password123");
        restTemplate.postForEntity("/auth/register", registerRequest, UserResponse.class);

        LoginRequest loginRequest = new LoginRequest("carol", "wrong-password");
        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity("/auth/login", loginRequest, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
