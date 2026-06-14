package com.cadence.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.cadence.auth.domain.Role;
import com.cadence.auth.security.jwt.JwtUtils;
import com.cadence.auth.security.service.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilsTest {

    private final JwtUtils jwtUtils = new JwtUtils();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "test-secret-key-that-is-at-least-32-characters-long");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60_000L);
    }

    @Test
    void generatesTokenThatCanBeValidatedAndParsed() {
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "alice", "hash", Role.MEMBER);

        String token = jwtUtils.generateToken(userDetails);

        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo("alice");
    }

    @Test
    void rejectsMalformedToken() {
        assertThat(jwtUtils.validateToken("not-a-valid-token")).isFalse();
    }
}
