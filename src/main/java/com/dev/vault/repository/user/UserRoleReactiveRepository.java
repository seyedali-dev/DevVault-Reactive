package com.dev.vault.repository.user;

import com.dev.vault.model.entity.user.UserRole;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface UserRoleReactiveRepository extends ReactiveMongoRepository<UserRole, String> {
    Flux<UserRole> findAllByUser_UserId(String userId);
}
