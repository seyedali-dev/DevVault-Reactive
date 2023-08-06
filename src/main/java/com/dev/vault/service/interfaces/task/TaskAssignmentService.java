package com.dev.vault.service.interfaces.task;

import com.dev.vault.helper.exception.*;
import com.dev.vault.helper.payload.response.task.TaskResponse;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Interface for assigning tasks services.
 */
public interface TaskAssignmentService {

    /**
     * Assigns a task to a list of users.
     *
     * @param taskId     The ID of the task to assign.
     * @param projectId  The ID of the project to which the task belongs.
     * @param userIdList The list of user IDs to assign the task to.
     * @return A {@link TaskResponse} containing information about the assigned task and its assigned users.
     * @throws ResourceNotFoundException   If the task or project is not found.
     * @throws DevVaultException           If the task does not belong to the project.
     * @throws NotLeaderOfProjectException If the current user is not a leader or admin of the project.
     */
    Mono<TaskResponse> assignTaskToUsers(String taskId, String projectId, List<String> userIdList)
            throws ResourceNotFoundException, NotLeaderOfProjectException, DevVaultException;


    /**
     * Assigns a task to all users in a project.
     *
     * @param taskId    The ID of the task to assign.
     * @param projectId The ID of the project to which the task belongs.
     * @return A {@link TaskResponse} containing information about the assigned task and its assigned users.
     * @throws NotLeaderOfProjectException If the current user is not a leader or admin of the project.
     * @throws ResourceNotFoundException   If the task or project is not found.
     */
    Mono<TaskResponse> assignTaskToAllUsersInProject(String taskId, String projectId)
            throws ResourceNotFoundException, NotLeaderOfProjectException;


    /**
     * Unassigns a task from a user in a given project.
     *
     * @param taskId    the ID of the task to unassign.
     * @param projectId the ID of the project containing the task.
     * @param userId    the ID of the user to unassign the task from.
     * @return a {@code Mono<Void>} that completes when the task has been unassigned.
     * @throws ResourceNotFoundException      if the task, project or the user with the given ID is not found.
     * @throws NotMemberOfProjectException    if the current user is not a member of the project.
     * @throws NotLeaderOfProjectException    if the current user is not the leader or admin of the project.
     */
    Mono<Void> unAssignTaskFromUser(String taskId, String projectId, String userId)
            throws ResourceNotFoundException, NotLeaderOfProjectException, NotMemberOfProjectException;


    /**
     * Unassigns a task from a list of users.
     *
     * @param taskId     The ID of the task to unassign.
     * @param projectId  The ID of the project to which the task belongs.
     * @param userIdList The list of user IDs to unassign the task from.
     */
    void unAssignTaskFromUsers(Long taskId, Long projectId, List<Long> userIdList);


    /**
     * Unassigns a task from all users in a project.
     *
     * @param taskId    The ID of the task to unassign.
     * @param projectId The ID of the project to which the task belongs.
     */
    void unassignTaskFromAllUsersInProject(Long taskId, Long projectId);

}
