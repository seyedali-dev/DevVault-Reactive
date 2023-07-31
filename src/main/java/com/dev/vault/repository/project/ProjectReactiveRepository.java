package com.dev.vault.repository.project;

import com.dev.vault.model.domain.project.Project;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProjectReactiveRepository extends ReactiveMongoRepository<Project, String> {
    Mono<Project> findByProjectName(String projectName);

    Flux<Project> findByProjectNameContaining(String projectName);

    Mono<Boolean> existsByProjectNameIgnoreCase(String projectName);

}