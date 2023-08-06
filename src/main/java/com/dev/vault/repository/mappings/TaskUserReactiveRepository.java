package com.dev.vault.repository.mappings;

import com.dev.vault.model.domain.relationship.TaskUser;
import com.dev.vault.model.domain.task.Task;
import com.dev.vault.model.domain.user.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskUserReactiveRepository extends ReactiveMongoRepository<TaskUser, String> {
    Mono<Boolean> existsByTaskAndUser(Task task, User user);

    Flux<TaskUser> findByUser_UserId(String userId);

    Flux<TaskUser> findByTask_TaskId(String taskId);

    Mono<TaskUser> findTaskUserByTask_TaskId(String taskId);

    Mono<TaskUser> findByTask_TaskIdAndUser_UserId(String taskId, String userId);

    Flux<TaskUser> findByUser_UserIdAndTask_TaskId(String userId, String taskId);
}
