package com.dev.vault.repository.user;

import com.dev.vault.model.entity.user.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserReactiveRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByEmail(String email);
}