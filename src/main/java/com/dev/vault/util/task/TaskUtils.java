package com.dev.vault.util.task;

import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.task.TaskRequest;
import com.dev.vault.helper.payload.response.task.TaskResponse;
import com.dev.vault.model.entity.mappings.TaskUser;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.task.Task;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.model.enums.TaskPriority;
import com.dev.vault.model.enums.TaskStatus;
import com.dev.vault.repository.mappings.TaskUserReactiveRepository;
import com.dev.vault.repository.task.TaskReactiveRepository;
import com.dev.vault.repository.user.UserReactiveRepository;
import com.dev.vault.service.module.task.TaskManagementServiceImpl;
import com.dev.vault.util.project.ProjectUtilsImpl;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dev.vault.model.enums.TaskStatus.IN_PROGRESS;


/**
 * A utility class that provides helper methods for working with tasks.
 * This class contains method for checking if a task with the same name already exists in a project. //TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskUtils {

    private final TaskUserReactiveRepository taskUserReactiveRepository;
    private final TaskReactiveRepository taskReactiveRepository;
    private final ModelMapper mapper;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;
    private final ProjectUtilsImpl projectUtils;
    private final UserReactiveRepository userReactiveRepository;


    /**
     * Checks if a task with the same name already exists in the projectId
     *
     * @param taskName  the request object containing the details of the task to create
     * @param projectId the projectId to check for existing tasks
     * @return Mono of true if a task with the same name already exists in the projectId, false otherwise
     */
    public Mono<Boolean> doesTaskAlreadyExists(String taskName, String projectId) {
        return taskReactiveRepository.existsByTaskNameAndProjectId(taskName, projectId);
    }


    /**
     * Builds a {@link TaskResponse} object.
     *
     * @param task the assigned task
     * @return a {@link TaskResponse} object with information about the newly created task
     */
    public Mono<TaskResponse> buildTaskResponse_ForCreatingTask(Task task, Project project) {
        return reactiveRepositoryUtils.findTaskUsersByTaskId_OrElseThrow_ResourceNotFoundException(task.getTaskId())
                .collectList().map(taskUsers -> {
                    Map<String, String> assignedUsersMap = new HashMap<>();
                    for (TaskUser taskUser : taskUsers) {
                        String username = taskUser.getUser().getUsername();
                        String userId = taskUser.getUser().getUserId();

                        assignedUsersMap.put(userId, username);
                    }

                    return TaskResponse.builder()
                            .taskName(task.getTaskName())
                            .projectName(project.getProjectName())
                            .taskStatus(task.getTaskStatus())
                            .dueDate(task.getDueDate())
                            .assignedUsers(assignedUsersMap)
                            .taskPriority(task.getTaskPriority())
                            .build();
                });
    }


    /**
     * Builds a {@link Task} object.
     *
     * @param project project.
     * @param user    user.
     * @return created {@link Task}.
     */
    public Task buildTaskObject(Project project, User user, TaskRequest taskRequest) {
        Task task = mapper.map(taskRequest, Task.class);
        task.setCreatedByUserId(user.getUserId());
        task.setProjectId(project.getProjectId());
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(IN_PROGRESS);

        TaskUser taskUser = new TaskUser();
        taskUser.setUser(user);
        taskUser.setTask(task);
        task.getAssignedTaskUser().add(taskUser);

        return task;
    }


    /**
     * Builds a {@link Task} object as response for {@link TaskManagementServiceImpl#searchTaskBasedOnDifferentCriteria(TaskStatus, TaskPriority, String, String)
     * searchTaskBasedOnDifferentCriteria(TaskStatus, TaskPriority, String, String)}
     *
     * @param task {@link Task}.
     * @return Mono of created {@link Task}.
     */
    public Mono<TaskResponse> buildTaskResponse_ForSearchTask(Task task) {
        return reactiveRepositoryUtils.findTaskUsersByTaskId_OrElseThrow_ResourceNotFoundException(task.getTaskId())
                .collectList().flatMap(taskUsers -> {
                    Map<String, String> assignedUsersMap = new HashMap<>();

                    for (TaskUser taskUser : taskUsers) {
                        String username = taskUser.getUser().getUsername();
                        String userId = taskUser.getUser().getUserId();

                        assignedUsersMap.put(userId, username);
                    }

                    return reactiveRepositoryUtils.findProjectById_OrElseThrow_ResourceNotFoundException(task.getProjectId())
                            .flatMap(project ->
                                    Mono.just(TaskResponse.builder()
                                            .taskName(task.getTaskName())
                                            .projectName(project.getProjectName())
                                            .taskStatus(task.getTaskStatus())
                                            .dueDate(task.getDueDate())
                                            .taskPriority(task.getTaskPriority())
                                            .assignedUsers(assignedUsersMap)
                                            .build()
                                    )
                            );
                });
    }


    /**
     * Builds a {@link TaskResponse} object for a given task and project, based on the status of task assignment
     * operation for the users. The {@code responseMap} parameter should contain the status messages for each user,
     * where the key is the username and the value is the status message.
     *
     * @param task        the task for which to build the response
     * @param project     the project to which the task belongs
     * @param responseMap a map containing the status messages for each user
     * @return a {@link Mono} that emits the {@link TaskResponse} object
     * @throws ResourceNotFoundException if the task or task users are not found in the database
     */
    public Mono<TaskResponse> buildTaskResponse_ForAssignTaskToUsers(Task task, Project project, Map<String, String> responseMap) {
        return reactiveRepositoryUtils.findTaskUsersByTaskId_OrElseThrow_ResourceNotFoundException(task.getTaskId())
                .collectList().flatMap(taskUsers -> Mono.just(
                                TaskResponse.builder()
                                        .taskName(task.getTaskName())
                                        .projectName(project.getProjectName())
                                        .taskStatus(task.getTaskStatus())
                                        .dueDate(task.getDueDate())
                                        .assignedUsers(responseMap).taskPriority(task.getTaskPriority())
                                        .build()
                        )
                );
    }


    /**
     * Assigns a task to a list of users and returns a map of user IDs to status messages.
     *
     * @param userIdList        the list of user IDs to assign the task to
     * @param task              the task to assign
     * @param project           the project the task belongs to
     * @param statusResponseMap the map to store the status messages for each user ID
     *                          The status message is:
     *                          <p>"Success: Task assigned to user [username]" if the task is successfully assigned, or
     *                          <p>"Fail: [reason]" if the task cannot be assigned.
     * @return a Mono that emits a map of user IDs to status messages
     * @throws RecourseNotFoundException if the user is not found from the userIdList.
     */
    @SuppressWarnings("JavadocReference")
    public Mono<Map<String, String>> assignTaskToUserList(List<String> userIdList, Task task, Project project, Map<String, String> statusResponseMap) {
        // Convert the list of user IDs into a Flux that emits each ID one by one
        return Flux.fromIterable(userIdList)

                // For each user ID, retrieve the corresponding User object and check if they are a member of the project
                .flatMap(userId -> reactiveRepositoryUtils.findUserById_OrElseThrow_ResourceNotFoundException(userId)
                                .flatMap(user -> projectUtils.isMemberOfProject(project, user)

                                                // If the user is not a member of the project, add a failure status message to the statusResponseMap
                                                .flatMap(isMember -> {
                                                    if (!isMember) {
                                                        statusResponseMap.put(user.getUsername(), "Fail: User with ID {" + userId + "} is not a member of Project with ID {" + project.getProjectId() + "}");
                                                        return Mono.empty();
                                                    } else {
                                                        return taskUserReactiveRepository.existsByTaskAndUser(task, user).flatMap(taskAlreadyAssigned -> {
                                                            if (taskAlreadyAssigned) {
                                                                statusResponseMap.put(user.getUsername(), "Fail: Task already assigned to user {" + user.getUsername() + "}");
                                                                return Mono.empty();
                                                            } else {
                                                                // Create a new TaskUser entity and set its User and Task fields
                                                                TaskUser taskUser = TaskUser.builder()
                                                                        .user(user)
                                                                        .task(task)
                                                                        .build();

                                                                // Save the TaskUser entity to the database
                                                                return taskUserReactiveRepository.save(taskUser)
                                                                        .flatMap(savedTaskUser -> {

                                                                            // Add the TaskUser entity to the Task and User entities
                                                                            task.getAssignedTaskUser().add(savedTaskUser);
                                                                            user.getTaskUsers().add(savedTaskUser);

                                                                            // Save the updated Task and User entities to the database
                                                                            return Mono.zip(
                                                                                    taskReactiveRepository.save(task),
                                                                                    userReactiveRepository.save(user)
                                                                            );
                                                                        }).doOnSuccess(ignored -> statusResponseMap.put(user.getUsername(), "Success: Task assigned to user {" + user.getUsername() + "}"))
                                                                        .onErrorResume(throwable -> {
                                                                            statusResponseMap.put(user.getUsername(), "Fail: Error assigning task to user {" + user.getUsername() + "}: " + throwable.getMessage());
                                                                            return Mono.empty();
                                                                        });
                                                            }
                                                        });
                                                    }
                                                })
                                )
                )// Once all the users have been processed, discard the emitted values and return a Mono that emits the updated statusResponseMap
                .then(Mono.just(statusResponseMap));
    }

}


/**
 * Retrieves a set of users associated with a task and a project, and updates the statusResponseMap with the status of the assignment for each user.
 *
 * @param task              The task to assign.
 * @param project           The project to which the task belongs.
 * @param statusResponseMap The map to which the status of the assignment for each user will be added.
 * @return A set of users associated with the task and the project.
 */
    /*public Set<User> getUsers(Task task, Project project, Map<String, String> statusResponseMap) {
        return projectMembersRepository.findByProject(project)
                .stream().map(projectMembers -> {
                    User user = reactiveRepositoryUtils.findUserById_OrElseThrow_ResourceNoFoundException(projectMembers.getUser().getUserId());
                    // Check if the task is already assigned to the user, skip ahead and add a response to the map if it is
                    if (taskReactiveRepository.findByAssignedUsersAndTaskId(user, task.getTaskId()).isPresent())
                        statusResponseMap.put(user.getUsername(), "Fail: Task already assigned to user " + user.getUsername());
                    else statusResponseMap.put(user.getUsername(), "Success");
                    user.getTask().add(task);

                    return userReactiveRepository.save(user);
                }).collect(Collectors.toSet());
    }*/
