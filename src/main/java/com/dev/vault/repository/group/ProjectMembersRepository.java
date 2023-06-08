package com.dev.vault.repository.group;

import com.dev.vault.model.group.Project;
import com.dev.vault.model.group.ProjectMembers;
import com.dev.vault.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProjectMembersRepository extends JpaRepository<ProjectMembers, Long> {
    @Query("""
            SELECT p FROM ProjectMembers p
            WHERE p.project =:project
            ORDER BY p.user.roles""")
    List<ProjectMembers> findByProject(Project project);

    Optional<ProjectMembers> findByProject_ProjectNameAndUser_Email(String projectName, String email);

    Optional<ProjectMembers> findByProjectAndUser(Project project, User user);
}