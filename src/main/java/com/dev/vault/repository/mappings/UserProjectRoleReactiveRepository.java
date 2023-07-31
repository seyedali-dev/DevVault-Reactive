package com.dev.vault.repository.mappings;

import com.dev.vault.model.domain.relationship.UserProjectRole;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserProjectRoleReactiveRepository extends ReactiveMongoRepository<UserProjectRole, String> {
    Mono<UserProjectRole> findByUserIdAndRoleIdAndProjectId(String userId, String roleId, String projectId);
}