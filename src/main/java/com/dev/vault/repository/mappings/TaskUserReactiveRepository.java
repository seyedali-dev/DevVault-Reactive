package com.dev.vault.repository.mappings;

import com.dev.vault.model.entity.mappings.TaskUser;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TaskUserReactiveRepository extends ReactiveMongoRepository<TaskUser, String> {
    Flux<TaskUser> findByUser_UserId(String userId);

    Flux<TaskUser> findByTask_TaskId(String taskId);
}
