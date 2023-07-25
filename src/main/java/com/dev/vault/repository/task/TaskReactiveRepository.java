package com.dev.vault.repository.task;

import com.dev.vault.model.entity.task.Task;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface TaskReactiveRepository extends ReactiveMongoRepository<Task, String> {
    Mono<Boolean> existsByTaskNameAndProjectId(String taskName, String projectId);
}