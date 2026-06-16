package com.cadence.auth.service;

import com.cadence.auth.domain.RefreshToken;
import com.cadence.auth.exception.InvalidRefreshTokenException;
import com.cadence.auth.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${cadence.auth.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public RefreshToken create(Long userId) {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .build();
        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyValid(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new InvalidRefreshTokenException("Refresh token expired");
        }
        return token;
    }

    public void deleteByToken(String tokenValue) {
        refreshTokenRepository.deleteByToken(tokenValue);
    }
}
