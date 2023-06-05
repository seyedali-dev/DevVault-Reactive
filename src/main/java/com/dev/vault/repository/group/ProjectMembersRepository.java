package com.dev.vault.repository.group;

import com.dev.vault.helper.payload.dto.ProjectMembersDto;
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
    List<ProjectMembers> findByProjectAndUser(Project project);
}