package com.dev.vault.repository.user.jwt;

import com.dev.vault.model.user.jwt.JwtToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.List;
import java.util.Optional;

public interface JwtTokenRepository extends ReactiveMongoRepository<JwtToken, Long> {
    List<JwtToken> findAllByUser_UserIdAndExpiredIsFalseAndRevokedIsFalse(Long userId);

    List<JwtToken> findAllByUser_UserId(Long userId);

    Optional<JwtToken> findByToken(String token);
}