package com.dev.vault.repository.mappings;

import com.dev.vault.model.domain.relationship.ProjectTask;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProjectTaskReactiveRepository extends ReactiveMongoRepository<ProjectTask, String> {
    Flux<ProjectTask> findByTask_TaskId(String taskId);
}
