package com.dev.vault.service.interfaces.task;

import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.NotMemberOfProjectException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.model.enums.TaskStatus;
import reactor.core.publisher.Mono;

/**
 * A service interface for managing task progress.
 */
public interface TaskProgressService {

    /**
     * Marks a task as completed.
     *
     * @param taskId     the ID of the task to mark as completed
     * @param projectId  the ID of the project to which the task belongs
     * @param taskStatus the status of the task
     * @return a Mono of Void representing the completion of the marking process
     * @throws ResourceNotFoundException   if the task or project is not found
     * @throws NotLeaderOfProjectException if the user is not a leader of the project
     * @throws NotMemberOfProjectException if the user is not a member of the project
     */
    Mono<Void> markTaskAsCompleted(String taskId, String projectId, TaskStatus taskStatus)
            throws ResourceNotFoundException, NotLeaderOfProjectException, NotMemberOfProjectException;

}
