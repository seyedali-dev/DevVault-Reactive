package com.dev.vault.service.interfaces.task;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.NotMemberOfProjectException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.payload.task.TaskResponse;

import java.util.List;

public interface TaskAssignmentService {

    /**
     * Assigns a task to a list of users.
     *
     * @param taskId     The ID of the task to assign.
     * @param projectId  The ID of the project to which the task belongs.
     * @param userIdList The list of user IDs to assign the task to.
     * @return A {@link TaskResponse} containing information about the assigned task and its assigned users.
     * @throws RecourseNotFoundException      If the task or project is not found.
     * @throws DevVaultException              If the task does not belong to the project.
     * @throws NotLeaderOfProjectException    If the current user is not a leader or admin of the project.
     * @throws ResourceAlreadyExistsException If the task is already assigned to a user.
     * @throws NotMemberOfProjectException    If the user is not a member of the project.
     */
    TaskResponse assignTaskToUsers(Long taskId, Long projectId, List<Long> userIdList);

    /**
     * Assigns a task to all users in a project.
     *
     * @param taskId    The ID of the task to assign.
     * @param projectId The ID of the project to which the task belongs.
     * @return A {@link TaskResponse} containing information about the assigned task and its assigned users.
     * @throws RecourseNotFoundException   If the task or project is not found.
     * @throws NotLeaderOfProjectException If the current user is not a leader or admin of the project.
     * @throws NotMemberOfProjectException If the user is not a member of the project.
     */
    TaskResponse assignTaskToAllUsersInProject(Long taskId, Long projectId);

    void unAssignTaskFromUser(Long taskId, Long projectId, Long userId);

    void unAssignTaskFromUsers(Long taskId, Long projectId, List<Long> userIdList);

    void unassignTaskFromAllUsersInProject(Long taskId, Long projectId);
}
