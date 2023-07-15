package com.dev.vault.repository.user;

import com.dev.vault.model.user.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.Optional;

public interface UserRepository extends ReactiveMongoRepository<User, Long> {
    Optional<User> findByEmail(String email);
}