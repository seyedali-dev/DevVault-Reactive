package com.dev.vault.repository.task;

import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.task.Task;
import com.dev.vault.model.entity.user.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.Optional;

public interface TaskRepository extends ReactiveMongoRepository<Task, Long> {
    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    Optional<Task> findByAssignedUsersAndTaskId(User assignedUsers, Long taskId);

    Optional<Task> findByProjectAndTaskName(Project project, String taskName);
}