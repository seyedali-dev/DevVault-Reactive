package com.dev.vault.repository.group;

import com.dev.vault.model.group.JoinProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JoinProjectRepository extends JpaRepository<JoinProject, Long> {
    Optional<JoinProject> findByProject_ProjectName(String projectName);
}