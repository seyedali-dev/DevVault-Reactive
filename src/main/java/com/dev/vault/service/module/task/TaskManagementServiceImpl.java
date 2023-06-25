package com.dev.vault.service.module.task;

import com.dev.vault.helper.exception.*;
import com.dev.vault.helper.payload.task.TaskRequest;
import com.dev.vault.helper.payload.task.TaskResponse;
import com.dev.vault.util.project.ProjectUtils;
import com.dev.vault.model.group.Project;
import com.dev.vault.model.task.Task;
import com.dev.vault.model.user.User;
import com.dev.vault.repository.task.TaskRepository;
import com.dev.vault.repository.user.UserRepository;
import com.dev.vault.service.interfaces.AuthenticationService;
import com.dev.vault.service.interfaces.TaskManagementService;
import com.dev.vault.util.repository.RepositoryUtils;
import com.dev.vault.util.task.TaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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

        return TaskResponse.builder()
                .taskName(task.getTaskName())
                .projectName(task.getProject().getProjectName())
                .taskStatus(task.getTaskStatus())
                .dueDate(task.getDueDate())
                .build();
    }

    /**
     * Assigns a task to the specified users for a given project.
     *
     * @param projectId  the ID of the project to assign the task to
     * @param taskId     the ID of the task to assign
     * @param userIdList a list of user IDs to assign the task to
     * @return the updated task object
     * @throws ResourceNotFoundException   if the project or task cannot be found
     * @throws NotLeaderOfProjectException if the requesting user is not leader or admin of the project
     */
//    @Override
//    @Transactional
//    public TaskResponse assignTaskToUsers(Long taskId, Long projectId, List<Long> userIdList) {
//        Task task = repositoryUtils.findTaskByIdOrElseThrowNotFoundException(taskId);
//        Project project = repositoryUtils.findProjectByIdOrElseThrowNoFoundException(projectId);
//        if (!task.getProject().getProjectId().equals(projectId))
//            throw new DevVaultException("Task with ID " + taskId + " does not belong to project with ID " + projectId);
//        List<User> users = userRepository.findAllById(userIdList);
//
//        User currentUser = authenticationService.getCurrentUser();
//        if (!projectUtils.isLeaderOrAdminOfProject(project, currentUser))
//            throw new NotLeaderOfProjectException("👮🏻You are not a leader or admin of this project👮🏻");
//
//        Map<String, String> map = new HashMap<>();
//        Set<User> assignUsers = new HashSet<>();
//        TaskResponse taskResponse = TaskResponse.builder().build();
//
//        for (User user : users) {
//            try {
//                if (taskRepository.findByAssignedUsers(user).isPresent())
//                    throw new ResourceAlreadyExistsException("Task", "AssignedUser", user.getUsername());
//                if (!projectUtils.isMemberOfProject(project, user))
//                    throw new NotMemberOfProjectException("Fail: User with userID: " + user.getUserId() + " is not a member of project with projectID: " + projectId);
//                user.getTask().add(task);
//                assignUsers.add(userRepository.save(user));
//                map.put(user.getUsername(), "Success: Task assigned to User: " + user.getUsername());
//                task.setAssignedUsers(assignUsers);
//
//                taskResponse.setTaskName(task.getTaskName());
//                taskResponse.setTaskStatus(task.getTaskStatus());
//                taskResponse.setDueDate(task.getDueDate());
//                taskResponse.setProjectName(project.getProjectName());
//                taskResponse.setAssignedUsers(map);
//
//                taskRepository.save(task);
//            } catch (Exception e) {
//                log.error("Error assigning task to user {}: {}", user.getUsername(), e.getMessage());
//                map.put(user.getUsername(), e.getMessage());
//                taskResponse.setAssignedUsers(map);
//            }
//        }
//        return taskResponse;
//    }

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
    @Override
    @Transactional
    public TaskResponse assignTaskToUsers(Long taskId, Long projectId, List<Long> userIdList) {
        // Find the task by ID or throw a RecourseNotFoundException if it doesn't exist
        Task task = repositoryUtils.findTaskByIdOrElseThrowNotFoundException(taskId);
        // Find the project by ID or throw a RecourseNotFoundException if it doesn't exist
        Project project = repositoryUtils.findProjectByIdOrElseThrowNoFoundException(projectId);
        // Check if the task belongs to the project or throw a DevVaultException if it doesn't
        if (!task.getProject().getProjectId().equals(projectId))
            throw new DevVaultException("Task with ID " + taskId + " does not belong to project with ID " + projectId);

        // Get the current user
        User currentUser = authenticationService.getCurrentUser();
        // Check if the current user is a leader or admin of the project, or throw a NotLeaderOfProjectException if they're not
        if (!projectUtils.isLeaderOrAdminOfProject(project, currentUser))
            throw new NotLeaderOfProjectException("👮🏻You are not a leader or admin of this project👮🏻");

        // Create a set to hold the assigned users and a map to hold the responses for each user
        Set<User> assignedUsers = new HashSet<>();
        Map<String, String> assignUsersMap = new HashMap<>();

        // Loop through the list of user IDs
        taskUtils.assignTaskToUserList(projectId, userIdList, task, project, assignedUsers, assignUsersMap);

        // Build and return a TaskResponse with information about the assigned task and its assigned users
        return taskUtils.buildTaskResponse(task, project, assignUsersMap);
    }

    @Override
    public TaskResponse assignTaskToAllUsers(Long taskId, Long projectId, List<Long> userIdList) {
        // TODO: stub created method
        return null;
    }
}
