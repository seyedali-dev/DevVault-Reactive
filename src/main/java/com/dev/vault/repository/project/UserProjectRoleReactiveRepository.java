package com.dev.vault.repository.project;

import com.dev.vault.model.entity.project.UserProjectRole;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UserProjectRoleReactiveRepository extends ReactiveMongoRepository<UserProjectRole, String> {
}