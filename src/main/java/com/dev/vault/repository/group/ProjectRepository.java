package com.dev.vault.repository.group;

import com.dev.vault.model.entity.project.Project;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends ReactiveMongoRepository<Project, Long> {
    Optional<Project> findByProjectName(String projectName);

    List<Project> findByProjectNameContaining(String projectName);

    Optional<Project> findByProjectNameAllIgnoreCase(String projectName);
}