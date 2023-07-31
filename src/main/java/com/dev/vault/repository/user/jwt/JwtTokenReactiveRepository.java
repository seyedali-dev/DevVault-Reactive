package com.dev.vault.repository.user.jwt;

import com.dev.vault.model.domain.user.jwt.JwtToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface JwtTokenReactiveRepository extends ReactiveMongoRepository<JwtToken, Long> {
    Flux<List<JwtToken>> findAllByUser_UserIdAndExpiredIsFalseAndRevokedIsFalse(String userId);

    List<JwtToken> findAllByUser_UserId(String userId);

    Mono<JwtToken> findByToken(String token);
}