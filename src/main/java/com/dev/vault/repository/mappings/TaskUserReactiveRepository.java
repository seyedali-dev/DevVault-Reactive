package com.dev.vault.repository.mappings;

import com.dev.vault.model.entity.mappings.TaskUser;
import com.dev.vault.model.entity.task.Task;
import com.dev.vault.model.entity.user.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskUserReactiveRepository extends ReactiveMongoRepository<TaskUser, String> {
    Mono<Boolean> existsByTaskAndUser(Task task, User user);
    Flux<TaskUser> findByUser_UserId(String userId);
    Flux<TaskUser> findByTask_TaskId(String taskId);
}
