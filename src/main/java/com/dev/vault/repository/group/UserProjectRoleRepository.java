package com.dev.vault.repository.group;

import com.dev.vault.model.project.Project;
import com.dev.vault.model.project.UserProjectRole;
import com.dev.vault.model.user.Roles;
import com.dev.vault.model.user.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.Optional;

public interface UserProjectRoleRepository extends ReactiveMongoRepository<UserProjectRole, Long> {
    Optional<UserProjectRole> findByUserAndProjectAndRole(User user, Project project, Roles role);
}