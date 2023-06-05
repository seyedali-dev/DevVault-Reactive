package com.dev.vault.repository.group;

import com.dev.vault.model.group.Project;
import com.dev.vault.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByProjectNameContaining(String projectName);

    Optional<Project> findByProjectNameAllIgnoreCase(String projectName);
}