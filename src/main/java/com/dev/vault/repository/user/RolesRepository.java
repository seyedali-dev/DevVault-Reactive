package com.dev.vault.repository.user;

import com.dev.vault.model.user.Roles;
import com.dev.vault.model.user.enums.Role;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.Optional;

public interface RolesRepository extends ReactiveMongoRepository<Roles, Long> {
    Optional<Roles> findByRole(Role role);
}