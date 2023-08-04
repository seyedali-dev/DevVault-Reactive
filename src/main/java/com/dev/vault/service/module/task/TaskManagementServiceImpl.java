package com.dev.vault.service.module.task;

import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.NotMemberOfProjectException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.task.TaskRequest;
import com.dev.vault.helper.payload.response.task.TaskResponse;
import com.dev.vault.model.domain.relationship.TaskUser;
import com.dev.vault.model.domain.task.Task;
import com.dev.vault.model.enums.TaskPriority;
import com.dev.vault.model.enums.TaskStatus;
import com.dev.vault.repository.mappings.ProjectTaskReactiveRepository;
import com.dev.vault.repository.mappings.TaskUserReactiveRepository;
import com.dev.vault.repository.task.TaskReactiveRepository;
import com.dev.vault.service.interfaces.task.TaskManagementService;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import com.dev.vault.util.project.ProjectUtils;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import com.dev.vault.util.task.TaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Service implementation for task management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskManagementServiceImpl implements TaskManagementService {

    private final ProjectTaskReactiveRepository projectTaskReactiveRepository;
    private final TaskUserReactiveRepository taskUserReactiveRepository;
    private final TaskReactiveRepository taskReactiveRepository;
    private final AuthenticationService authenticationService;
    private final ProjectUtils projectUtils;
    private final TaskUtils taskUtils;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;
    private final ModelMapper modelMapper;


    /**
     * Creates a new task for a given project.
     *
     * @param projectId   the ID of the project to create the task for.
     * @param taskRequest the request object containing the details of the task to create.
     * @return a {@code Mono<TaskResponse>} object containing the details of the created task.
     * @throws ResourceNotFoundException      if the project with the given ID is not found.
     * @throws ResourceAlreadyExistsException if a task with the same name already exists in the project.
     * @throws NotMemberOfProjectException    if the current user is not a member of the project.
     * @throws NotLeaderOfProjectException    if the current user is not the leader or admin of the project.
     */
    @Override
    @Transactional
    public Mono<TaskResponse> createNewTask(String projectId, TaskRequest taskRequest) {
        // find the user and project
        return reactiveRepositoryUtils.find_ProjectById_OrElseThrow_ResourceNotFoundException(projectId).flatMap(project ->
                authenticationService.getCurrentUserMono().flatMap(currentUser ->

                        // Check if a task with the same name already exists in the project
                        taskUtils.doesTaskAlreadyExists(taskRequest.getTaskName(), project.getProjectId())
                                .flatMap(taskExists -> taskUtils.handleTaskExists(taskExists, taskRequest))

                                // Check if the currentUser is a member of the project
                                .flatMap(taskExists -> projectUtils.isMemberOfProject(project, currentUser))
                                .flatMap(isMemberOfProject -> taskUtils.handleUserMembership(isMemberOfProject, project, currentUser))

                                // Check if the currentUser is the leader or admin of the project
                                .flatMap(isMemberOfProject -> projectUtils.isLeaderOrAdminOfProject(project, currentUser))
                                .flatMap(isLeaderOrAdminOfProject -> taskUtils.handleUserLeadership(isLeaderOrAdminOfProject, project, currentUser))

                                // save `Task`, `TaskUser` and `ProjectTask` entities
                                .flatMap(isLeaderOrAdminOfProject -> taskUtils.saveTaskAndEntities(project, currentUser, taskRequest))
                                .flatMap(savedTask -> taskUtils.buildTaskResponse_ForCreatingTask(savedTask, project))
                )
        );
    }


    /**
     * Searches for tasks based on the given criteria and returns a {@code Flux<TaskResponse>} of matching tasks.
     *
     * @param status            the status of the tasks to search for (optional)
     * @param priority          the priority of the tasks to search for (optional)
     * @param projectId         the ID of the project that the tasks belong to (optional)
     * @param assignedTo_UserId the ID of the user that the tasks are assigned to (optional)
     * @return a {@code Flux<TaskResponse>} of matching tasks
     */
    public Flux<TaskResponse> searchTaskBasedOnDifferentCriteria(TaskStatus status, TaskPriority priority, String projectId, String assignedTo_UserId) {
        Flux<Task> taskFlux = Flux.empty();

        if (status != null)
            taskFlux = taskFlux.mergeWith(taskReactiveRepository.findByTaskStatus(status));

        if (priority != null)
            taskFlux = taskFlux.mergeWith(taskReactiveRepository.findByTaskPriority(priority));

        if (projectId != null)
            taskFlux = taskFlux.mergeWith(taskReactiveRepository.findByProjectId(projectId));

        if (assignedTo_UserId != null) {
            taskFlux = taskFlux.mergeWith(
                    taskUserReactiveRepository.findByUser_UserId(assignedTo_UserId)
                            .map(TaskUser::getTask)
            );
        }

        return taskFlux
//                .distinct() // if we don't the duplicate values to be sent as response
                .flatMap(taskUtils::buildTaskResponse_ForSearchTask);
    }


    /**
     * Updates the details of a task.
     *
     * @param taskId      the ID of the task to update
     * @param taskRequest the new task details
     * @return a Mono emitting the updated TaskResponse object
     * @throws ResourceNotFoundException if the task or associated project is not found
     */
    @Override
    public Mono<TaskResponse> updateTaskDetails(String taskId, TaskRequest taskRequest) {
        // find the task with the given ID
        return reactiveRepositoryUtils.find_TaskById_OrElseThrow_ResourceNotFoundException(taskId)
                .flatMap(task -> taskUtils.updateTask(task, taskRequest));
    }

}
