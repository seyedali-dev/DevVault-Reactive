/*
package com.dev.vault.service.module.task;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.task.TaskRequest;
import com.dev.vault.helper.payload.response.task.TaskResponse;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.task.Task;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.repository.task.TaskRepository;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import com.dev.vault.service.interfaces.task.TaskManagementService;
import com.dev.vault.util.project.ProjectUtils;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import com.dev.vault.util.task.TaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.dev.vault.model.entity.enums.task.TaskStatus.IN_PROGRESS;

*/
/**
 * Service implementation for task management.
 *//*

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskManagementServiceImpl implements TaskManagementService {
    private final TaskRepository taskRepository;
    private final ModelMapper mapper;
    private final AuthenticationService authenticationService;
    private final ProjectUtils projectUtils;
    private final TaskUtils taskUtils;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;

    */
/**
     * Creates a new task for a given project.
     *
     * @param projectId   the ID of the project to create the task for
     * @param taskRequest the request object containing the details of the task to create
     * @return a TaskResponse object containing the details of the created task
     * @throws ResourceNotFoundException      if the project with the given ID is not found
     * @throws ResourceAlreadyExistsException if a task with the same name already exists in the project
     *//*

    @Override
    @Transactional
    public TaskResponse createNewTask(Long projectId, TaskRequest taskRequest) {
        Project project = reactiveRepositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(projectId);
        User currentUser = authenticationService.getCurrentUser();

        // Check if a task with the same name already exists in the project
        if (taskUtils.doesTaskAlreadyExists(taskRequest, project))
            throw new ResourceAlreadyExistsException("Task", "TaskName", taskRequest.getTaskName());

        // Check if the currentUser is a member of the project
        if (!projectUtils.isMemberOfProject(project, currentUser))
            throw new DevVaultException("You are not a member of this project");

        // Check if the currentUser is the leader or admin of the project
        if (!projectUtils.isLeaderOrAdminOfProject(project, currentUser))
            throw new NotLeaderOfProjectException("👮🏻only leader and admin can create task!👮🏻");

        Task task = mapper.map(taskRequest, Task.class);
        task.setCreatedBy(currentUser);
        task.setProject(project);
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(IN_PROGRESS);
        task.getAssignedUsers().add(currentUser);
        taskRepository.save(task);

        return taskUtils.buildTaskResponse(task);
    }
}
*/
