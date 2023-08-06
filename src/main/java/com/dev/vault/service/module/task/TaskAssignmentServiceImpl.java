package com.dev.vault.service.module.task;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.NotMemberOfProjectException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.response.task.TaskResponse;
import com.dev.vault.repository.mappings.TaskUserReactiveRepository;
import com.dev.vault.service.interfaces.task.TaskAssignmentService;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import com.dev.vault.util.project.ProjectUtils;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import com.dev.vault.util.task.TaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for task assignments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskAssignmentServiceImpl implements TaskAssignmentService {

    private final TaskUserReactiveRepository taskUserReactiveRepository;
    private final AuthenticationService authenticationService;
    private final ProjectUtils projectUtils;
    private final TaskUtils taskUtils;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;


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
    @Override
    @Transactional
    public Mono<TaskResponse> assignTaskToUsers(String taskId, String projectId, List<String> userIdList) throws ResourceNotFoundException, NotLeaderOfProjectException, DevVaultException {
        // find the task and the project of that task
        return reactiveRepositoryUtils.find_TaskById_OrElseThrow_ResourceNotFoundException(taskId)
                .flatMap(task -> reactiveRepositoryUtils.find_ProjectById_OrElseThrow_ResourceNotFoundException(projectId)
                        .flatMap(project -> {

                            // Check if the task belongs to the project
                            if (!task.getProjectId().equals(projectId)) {
                                log.error("task does not belong to this project");
                                return Mono.error(new DevVaultException("Task with ID " + taskId + " does not belong to project with ID " + projectId));
                            }

                            return authenticationService.getCurrentUserMono().flatMap(currentUser -> {

                                // Check if the requesting user is leader or admin of the project
                                return projectUtils.isLeaderOrAdminOfProject(project, currentUser).flatMap(isLeader -> {
                                    if (!isLeader)
                                        return Mono.error(new NotLeaderOfProjectException("👮🏻You are not a leader or admin of this project👮🏻"));
                                    else {
                                        Map<String, String> statusResponseMap = new HashMap<>();

                                        // Loop through the list of user IDs and assign the task to them
                                        return taskUtils.assignTaskToUserList(userIdList, task, project, statusResponseMap)
                                                .then(taskUtils.buildTaskResponse_ForAssignTaskToUsers(task, project, statusResponseMap));
                                    }
                                });
                            });
                        })
                );
    }


    /**
     * Assigns a task to all users in a project.
     *
     * @param taskId    The ID of the task to assign.
     * @param projectId The ID of the project to which the task belongs.
     * @return A {@link TaskResponse} containing information about the assigned task and its assigned users.
     * @throws NotLeaderOfProjectException If the current user is not a leader or admin of the project.
     * @throws ResourceNotFoundException   If the task or project is not found.
     */
    @Override
    @Transactional
    public Mono<TaskResponse> assignTaskToAllUsersInProject(String taskId, String projectId) {
        // find the `task`, `project` and get the current `user`
        return reactiveRepositoryUtils.find_TaskById_OrElseThrow_ResourceNotFoundException(taskId).flatMap(task ->
                reactiveRepositoryUtils.find_ProjectById_OrElseThrow_ResourceNotFoundException(projectId).flatMap(project ->
                        authenticationService.getCurrentUserMono().flatMap(currentUser -> {

                            // Validate task and project :: whether they belong to each other
                            return taskUtils.validateTaskAndProject(task, project, currentUser)
                                    .then(Mono.defer(() -> {

                                                // Create a responseMap to hold the responses for each user
                                                Map<String, String> responseMap = new HashMap<>();

                                                // Retrieve a set of users associated with a task and a project, and updates the responseMap with the status of the assignment for each user.
                                                return taskUtils.assignTaskToUsersInProject(task, project, responseMap)
                                                        .collectList().flatMap(taskUsers -> {

                                                            // Build and return a TaskResponse with information about the assigned task and its assigned users
                                                            return taskUtils.buildTaskResponse_ForAssignTaskToUsers(task, project, responseMap);
                                                        });
                                            })
                                    );
                        })
                )
        );
    }


    /**
     * Unassigns a task from a user in a given project.
     *
     * @param taskId    the ID of the task to unassign.
     * @param projectId the ID of the project containing the task.
     * @param userId    the ID of the user to unassign the task from.
     * @return a {@code Mono<Void>} that completes when the task has been unassigned.
     * @throws ResourceNotFoundException   if the task, project or the user with the given ID is not found.
     * @throws NotMemberOfProjectException if the current user is not a member of the project.
     * @throws NotLeaderOfProjectException if the current user is not the leader or admin of the project.
     */
    @Override
    @Transactional
    public Mono<Void> unAssignTaskFromUser(String taskId, String projectId, String userId) {
        // to unassign a task from the user, we need to remove the taskUser object from db
        return reactiveRepositoryUtils.find_ProjectById_OrElseThrow_ResourceNotFoundException(projectId)
                .flatMap(project -> authenticationService.getCurrentUserMono()

                        // put the new details of task into the found task
                        .flatMap(user -> projectUtils.isMemberOfProject(project, user)
                                .flatMap(isMemberOfProject -> taskUtils.handleUserMembership(isMemberOfProject, project, user))
                                .flatMap(isMemberOfProject -> projectUtils.isLeaderOrAdminOfProject(project, user))
                                .flatMap(isLeaderOrAdminOfProject -> taskUtils.handleUserLeadership(isLeaderOrAdminOfProject, project, user))
                                .flatMap(isLeaderOrAdminOfProject -> reactiveRepositoryUtils.find_TaskUserByTaskAndUserId_OrElseThrow_ResourceNotFoundException(taskId, userId)
                                        .flatMap(taskUserReactiveRepository::delete)
                                )
                        )
                );

    }


    @Override
    public void unAssignTaskFromUsers(Long taskId, Long projectId, List<Long> userIdList) {
        //TODO
    }


    @Override
    public void unassignTaskFromAllUsersInProject(Long taskId, Long projectId) {
        //TODO
    }

}
