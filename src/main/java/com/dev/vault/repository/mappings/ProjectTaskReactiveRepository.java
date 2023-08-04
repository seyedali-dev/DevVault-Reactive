package com.dev.vault.repository.mappings;

import com.dev.vault.model.domain.relationship.ProjectTask;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProjectTaskReactiveRepository extends ReactiveMongoRepository<ProjectTask, String> {
    Mono<ProjectTask> findProjectTaskByTask_TaskId(String taskId);
    Flux<ProjectTask> findByTask_TaskId(String taskId);
}
