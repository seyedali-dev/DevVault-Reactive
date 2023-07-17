package com.dev.vault.repository.group;

import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.project.UserProjectRole;
import com.dev.vault.model.entity.user.Roles;
import com.dev.vault.model.entity.user.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.Optional;

public interface UserProjectRoleRepository extends ReactiveMongoRepository<UserProjectRole, Long> {
    Optional<UserProjectRole> findByUserAndProjectAndRole(User user, Project project, Roles role);
}