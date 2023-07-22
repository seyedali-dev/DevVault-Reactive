package com.dev.vault.repository.project;

import com.dev.vault.model.entity.project.UserProjectRole;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserProjectRoleReactiveRepository extends ReactiveMongoRepository<UserProjectRole, String> {
    Mono<UserProjectRole> findByUserIdAndRoleIdAndProjectId(String userId, String roleId, String projectId);
}