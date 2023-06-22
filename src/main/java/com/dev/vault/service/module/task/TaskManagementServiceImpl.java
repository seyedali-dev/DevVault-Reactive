package com.dev.vault.service.module.task;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.task.TaskRequest;
import com.dev.vault.helper.payload.task.TaskResponse;
import com.dev.vault.model.group.Project;
import com.dev.vault.model.group.ProjectMembers;
import com.dev.vault.model.group.UserProjectRole;
import com.dev.vault.model.task.Task;
import com.dev.vault.model.user.Roles;
import com.dev.vault.model.user.User;
import com.dev.vault.model.user.enums.Role;
import com.dev.vault.repository.group.ProjectMembersRepository;
import com.dev.vault.repository.group.ProjectRepository;
import com.dev.vault.repository.group.UserProjectRoleRepository;
import com.dev.vault.repository.task.TaskRepository;
import com.dev.vault.repository.user.UserRepository;
import com.dev.vault.service.AuthenticationService;
import com.dev.vault.service.TaskManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.dev.vault.model.task.enums.TaskStatus.IN_PROGRESS;

/**
 * Service implementation for task management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskManagementServiceImpl implements TaskManagementService {
    private final UserProjectRoleRepository userProjectRoleRepository;
    private final ProjectMembersRepository projectMembersRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ModelMapper mapper;
    private final AuthenticationService authenticationService;

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
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ProjectID", projectId.toString()));

        User user = authenticationService.getCurrentUser();

        // Check if the user is a member of project
        if (!isMemberOfProject(project, user))
            throw new DevVaultException("You are not a member of this project");

        // Check if the user is the leader or admin of project
        if (!isLeaderOrAdminOfProject(project, user))
            throw new NotLeaderOfProjectException("👮🏻only leader and admin can create task!👮🏻");

        // Check if a task with the same name already exists in the project
        if (doesTaskAlreadyExists(taskRequest, project))
            throw new ResourceAlreadyExistsException("Task", "TaskName", taskRequest.getTaskName());

        Task task = mapper.map(taskRequest, Task.class);
        task.setCreatedBy(user);
        task.setProject(project);
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(IN_PROGRESS);

        user.getTask().add(task);

        userRepository.save(user);
        taskRepository.save(task);

        return TaskResponse.builder()
                .taskName(task.getTaskName())
                .projectName(task.getProject().getProjectName())
                .taskStatus(task.getTaskStatus())
                .dueDate(task.getDueDate())
                .build();
    }

    // todo:: dry!!! repeating!!!
    private boolean isLeaderOrAdminOfProject(Project project, User user) {
        Roles role = user.getRoles().stream()
                .filter(roles ->
                        roles.getRole().equals(Role.PROJECT_LEADER) ||
                        roles.getRole().equals(Role.PROJECT_ADMIN)
                ).findFirst()
                .orElse(null);
        Optional<UserProjectRole> userProjectRole = userProjectRoleRepository.findByUserAndProjectAndRole(user, project, role);
        return userProjectRole.isPresent() &&
               (userProjectRole.get().getRole().getRole() == Role.PROJECT_LEADER ||
                userProjectRole.get().getRole().getRole() == Role.PROJECT_ADMIN);
    }

    private boolean doesTaskAlreadyExists(TaskRequest taskRequest, Project project) {
        Optional<Task> foundTask = taskRepository.findByProjectAndTaskName(project, taskRequest.getTaskName());
        return foundTask.isPresent();
    }

    private boolean isMemberOfProject(Project project, User user) {
        Optional<ProjectMembers> members = projectMembersRepository.findByProject_ProjectNameAndUser_Email(project.getProjectName(), user.getEmail());
        return members.isPresent();
    }
}
