package com.dev.vault.repository.task;

import com.dev.vault.model.domain.task.Task;
import com.dev.vault.model.enums.TaskPriority;
import com.dev.vault.model.enums.TaskStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskReactiveRepository extends ReactiveMongoRepository<Task, String> {
    Flux<Task> findByProjectId(String projectId);

    Flux<Task> findByTaskPriority(TaskPriority taskPriority);

    Flux<Task> findByTaskStatus(TaskStatus taskStatus);

    Mono<Boolean> existsByTaskNameAndProjectId(String taskName, String projectId);
}