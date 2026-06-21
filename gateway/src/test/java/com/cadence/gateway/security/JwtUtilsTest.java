package com.cadence.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha256";

    private JwtUtils jwtUtils;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(SECRET);
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void isValid_returnsTrue_forValidToken() {
        String token = buildToken(60_000);
        assertThat(jwtUtils.isValid(token)).isTrue();
    }

    @Test
    void isValid_returnsFalse_forExpiredToken() {
        String token = buildToken(-1_000);
        assertThat(jwtUtils.isValid(token)).isFalse();
    }

    @Test
    void isValid_returnsFalse_forTamperedToken() {
        String token = buildToken(60_000) + "tampered";
        assertThat(jwtUtils.isValid(token)).isFalse();
    }

    @Test
    void isValid_returnsFalse_forTokenSignedWithDifferentSecret() {
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "different-secret-key-that-is-also-long-enough-xx".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("testuser")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey)
                .compact();
        assertThat(jwtUtils.isValid(token)).isFalse();
    }

    private String buildToken(long offsetMs) {
        return Jwts.builder()
                .subject("testuser")
                .expiration(new Date(System.currentTimeMillis() + offsetMs))
                .signWith(key)
                .compact();
    }
}
