package com.dev.vault.repository.task;

import com.dev.vault.model.project.Project;
import com.dev.vault.model.task.Task;
import com.dev.vault.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByAssignedUsersAndTaskId(User assignedUsers, Long taskId);
    Optional<Task> findByProjectAndTaskName(Project project, String taskName);
}