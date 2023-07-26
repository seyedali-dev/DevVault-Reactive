package com.dev.vault.service.module.task;

import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.NotMemberOfProjectException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.task.TaskRequest;
import com.dev.vault.helper.payload.response.task.TaskResponse;
import com.dev.vault.model.entity.task.Task;
import com.dev.vault.model.enums.TaskPriority;
import com.dev.vault.model.enums.TaskStatus;
import com.dev.vault.repository.task.TaskReactiveRepository;
import com.dev.vault.service.interfaces.task.TaskManagementService;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import com.dev.vault.util.project.ProjectUtils;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import com.dev.vault.util.task.TaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;


/**
 * Service implementation for task management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskManagementServiceImpl implements TaskManagementService {

    private final TaskReactiveRepository taskReactiveRepository;
    private final AuthenticationService authenticationService;
    private final ProjectUtils projectUtils;
    private final TaskUtils taskUtils;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;


    /**
     * Creates a new task for a given project.
     *
     * @param projectId   the ID of the project to create the task for
     * @param taskRequest the request object containing the details of the task to create
     * @return a TaskResponse object containing the details of the created task
     * @throws ResourceNotFoundException      if the project with the given ID is not found
     * @throws ResourceAlreadyExistsException if a task with the same name already exists in the project
     */
    @Override
    @Transactional
    public Mono<TaskResponse> createNewTask(String projectId, TaskRequest taskRequest) {
        // find the user and project
        return reactiveRepositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(projectId).flatMap(project ->
                authenticationService.getCurrentUserMono().flatMap(currentUser -> {

                    // Check if a task with the same name already exists in the project
                    return taskUtils.doesTaskAlreadyExists(taskRequest.getTaskName(), project.getProjectId())
                            .flatMap(taskExists -> {
                                log.info("projectID: {{}}, taskName: {{}}", project.getProjectId(), taskRequest.getTaskName());
                                log.info("taskExists?: {{}}", taskExists);
                                if (taskExists) {
                                    log.error("Task already Exists: taskResource - {{}}", taskRequest.getTaskName());
                                    return Mono.error(new ResourceAlreadyExistsException("Task", "TaskName", taskRequest.getTaskName()));
                                }

                                // Check if the currentUser is a member of the project
                                return projectUtils.isMemberOfProject(project, currentUser)
                                        .flatMap(isMemberOfProject -> {
                                            if (!isMemberOfProject) {
                                                log.error("You are not a member of this project: {{}} - user: {{}}", project.getProjectName(), currentUser.getUsername());
                                                return Mono.error(new NotMemberOfProjectException("You are not a member of this project"));
                                            }

                                            // Check if the currentUser is the leader or admin of the project
                                            return projectUtils.isLeaderOrAdminOfProject(project, currentUser)
                                                    .flatMap(isLeaderOrAdminOfProject -> {
                                                        if (!isLeaderOrAdminOfProject) {
                                                            log.error("👮🏻only leader and admin can create task!👮🏻: {{}} - user: {{}}", project.getProjectName(), currentUser.getUsername());
                                                            return Mono.error(new NotLeaderOfProjectException("👮🏻only leader and admin can create task!👮🏻"));
                                                        }

                                                        return taskReactiveRepository.save(taskUtils.buildTaskObject(project, currentUser, taskRequest))
                                                                .map(savedTask -> taskUtils.buildTaskResponse(savedTask, project));

                                                    });
                                        });
                            });
                })
        );
    }

    /**
     * Searches for tasks based on the given criteria and returns a Flux of {@link TaskResponse} objects.
     *
     * @param status            the status of the tasks to search for (optional)
     * @param priority          the priority of the tasks to search for (optional)
     * @param projectId         the ID of the project that the tasks belong to (optional)
     * @param assignedTo_UserId the ID of the user that the tasks are assigned to (optional)
     * @return a Flux of {@link TaskResponse} objects
     */
    public Flux<TaskResponse> searchTaskBasedOnDifferentCriteria(TaskStatus status, TaskPriority priority, String projectId, String assignedTo_UserId) {
        Flux<Task> taskFlux = Flux.empty();

        if (status != null)
            taskFlux = taskFlux.mergeWith(taskReactiveRepository.findByTaskStatus(status));

        if (priority != null)
            taskFlux = taskFlux.mergeWith(taskReactiveRepository.findByTaskPriority(priority));

        if (projectId != null)
            taskFlux = taskFlux.mergeWith(taskReactiveRepository.findByProjectId(projectId));

        if (assignedTo_UserId != null)
            taskFlux = taskFlux.mergeWith(taskReactiveRepository.findByAssignedUserIds(Set.of(assignedTo_UserId)));

        return taskFlux
//                .distinct() // if we don't the duplicate values to be sent as response
                .flatMap(taskUtils::buildTaskResponse);
    }

}
