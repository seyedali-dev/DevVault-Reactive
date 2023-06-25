package com.dev.vault.service.module.task;

import com.dev.vault.helper.exception.*;
import com.dev.vault.helper.payload.task.TaskRequest;
import com.dev.vault.helper.payload.task.TaskResponse;
import com.dev.vault.model.group.Project;
import com.dev.vault.model.task.Task;
import com.dev.vault.model.user.User;
import com.dev.vault.repository.group.ProjectMembersRepository;
import com.dev.vault.repository.task.TaskRepository;
import com.dev.vault.repository.user.UserRepository;
import com.dev.vault.service.interfaces.AuthenticationService;
import com.dev.vault.service.interfaces.TaskManagementService;
import com.dev.vault.util.project.ProjectUtils;
import com.dev.vault.util.repository.RepositoryUtils;
import com.dev.vault.util.task.TaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.dev.vault.model.task.enums.TaskStatus.IN_PROGRESS;

/**
 * Service implementation for task management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskManagementServiceImpl implements TaskManagementService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ModelMapper mapper;
    private final AuthenticationService authenticationService;
    private final ProjectUtils projectUtils;
    private final TaskUtils taskUtils;
    private final RepositoryUtils repositoryUtils;

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
    public TaskResponse createNewTask(Long projectId, TaskRequest taskRequest) {
        // Find the project with the given ID
        Project project = repositoryUtils.findProjectByIdOrElseThrowNoFoundException(projectId);

        User user = authenticationService.getCurrentUser();

        // Check if the user is a member of the project
        if (!projectUtils.isMemberOfProject(project, user))
            throw new DevVaultException("You are not a member of this project");

        // Check if the user is the leader or admin of the project
        if (!projectUtils.isLeaderOrAdminOfProject(project, user))
            throw new NotLeaderOfProjectException("👮🏻only leader and admin can create task!👮🏻");

        // Check if a task with the same name already exists in the project
        if (taskUtils.doesTaskAlreadyExists(taskRequest, project))
            throw new ResourceAlreadyExistsException("Task", "TaskName", taskRequest.getTaskName());

        Task task = mapper.map(taskRequest, Task.class);
        task.setCreatedBy(user);
        task.setProject(project);
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(IN_PROGRESS);

        // Add the task to the user's task list
        user.getTask().add(task);

        userRepository.save(user);
        taskRepository.save(task);

        return taskUtils.buildTaskResponse(task);
    }
}
