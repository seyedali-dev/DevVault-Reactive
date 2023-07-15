package com.dev.vault.repository.user;

import com.dev.vault.model.user.VerificationToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends ReactiveMongoRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
}