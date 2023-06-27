package com.dev.vault.repository.group;

import com.dev.vault.model.project.Project;
import com.dev.vault.model.project.UserProjectRole;
import com.dev.vault.model.user.Roles;
import com.dev.vault.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProjectRoleRepository extends JpaRepository<UserProjectRole, Long> {
    Optional<UserProjectRole> findByUserAndProjectAndRole(User user, Project project, Roles role);
}