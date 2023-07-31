package com.dev.vault.util.task;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.NotMemberOfProjectException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.task.TaskRequest;
import com.dev.vault.helper.payload.response.task.TaskResponse;
import com.dev.vault.model.domain.project.Project;
import com.dev.vault.model.domain.relationship.TaskUser;
import com.dev.vault.model.domain.task.Task;
import com.dev.vault.model.domain.user.User;
import com.dev.vault.model.enums.TaskPriority;
import com.dev.vault.model.enums.TaskStatus;
import com.dev.vault.repository.mappings.ProjectMembersReactiveRepository;
import com.dev.vault.repository.mappings.ProjectTaskReactiveRepository;
import com.dev.vault.repository.mappings.TaskUserReactiveRepository;
import com.dev.vault.repository.project.ProjectReactiveRepository;
import com.dev.vault.repository.task.TaskReactiveRepository;
import com.dev.vault.repository.user.UserReactiveRepository;
import com.dev.vault.service.module.task.TaskAssignmentServiceImpl;
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
 * This class contains methods for:
 * <ul>
 *     <li>Assigning a {@link Task} to a list of users and returns a map of user IDs to status messages.</li>
 *     <li>Assigning a {@link Task} to all members of a project and returns a Flux of TaskUser.</li>
 *     <li>Checks if a {@link Task} with the same name already exists in the project.</li>
 *     <li>Build a {@link TaskResponse} object as response for {@link TaskManagementServiceImpl#createNewTask(String, TaskRequest) createNewTask(String projectId, TaskRequest taskRequest)}.</li>
 *     <li>Build a {@link Task} object as response for {@link TaskManagementServiceImpl#searchTaskBasedOnDifferentCriteria(TaskStatus, TaskPriority, String, String)
 *      searchTaskBasedOnDifferentCriteria(TaskStatus taskStatus, TaskPriority taskPriority, String projectId, String assignedTo_UserId)}.</li>
 *      <li>Build a {@link TaskResponse} object for the given task and project, based on the response map from assigning the task to users.
 *      For {@link TaskAssignmentServiceImpl#assignTaskToUsers(String, String, List) assignTaskToUsers(String taskId, String projectId, List&lt;String&gt; userIdList)}.</li>
 *      <li>Build a {@link Task} object.</li>
 *      <li>Validate whether the {@link Task} belongs to the {@link Project} and whether the {@link User} is a member and leader/admin of the project.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskUtils {

    private final ProjectTaskReactiveRepository projectTaskReactiveRepository;
    private final ProjectReactiveRepository projectReactiveRepository;
    private final ProjectMembersReactiveRepository projectMembersReactiveRepository;
    private final TaskUserReactiveRepository taskUserReactiveRepository;
    private final TaskReactiveRepository taskReactiveRepository;
    private final ModelMapper mapper;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;
    private final ProjectUtilsImpl projectUtils;
    private final UserReactiveRepository userReactiveRepository;


    /**
     * Assigns a task to a list of users and returns a map of user IDs to status messages.
     *
     * @param userIdList        The list of user IDs to assign the task to.
     * @param task              The task to assign.
     * @param project           The project the task belongs to.
     * @param statusResponseMap The map to store the status messages for each user ID. The status message is one of:
     *                          <ul>
     *                              <li>"Success: Task assigned to user [username]" if the task is successfully assigned</li>
     *                              <li>"Fail: [reason]" if the task cannot be assigned</li>
     *                          </ul>
     * @return A {@link Mono} that emits a map of user IDs to status messages.
     * @throws ResourceNotFoundException If a user is not found from the {@code userIdList}.
     */
    public Mono<Map<String, String>> assignTaskToUserList(List<String> userIdList, Task task, Project project, Map<String, String> statusResponseMap) {
        // Convert the list of user IDs into a Flux that emits each ID one by one
        return Flux.fromIterable(userIdList)

                // For each user ID, retrieve the corresponding User object and check if they are a member of the project
                .flatMap(userId -> reactiveRepositoryUtils.find_UserById_OrElseThrow_ResourceNotFoundException(userId)
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
                                                // Create a new TaskUser domain and set its User and Task fields
                                                TaskUser taskUser = TaskUser.builder()
                                                        .user(user)
                                                        .task(task)
                                                        .build();

                                                // Save the TaskUser domain to the database
                                                return taskUserReactiveRepository.save(taskUser)
                                                        .flatMap(savedTaskUser -> {

                                                            // Add the TaskUser domain to the Task and User entities
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


    /**
     * Assigns a task to all members of a project and returns a Flux of {@link TaskUser}.
     *
     * @param task              the task to be assigned.
     * @param project           the project to assign the task to.
     * @param statusResponseMap a map used to store the status of each user assignment.
     * @return a {@link Flux} of {@link TaskUser} representing the assignment of the task to users.
     * @throws ResourceNotFoundException if {@link User} associated with a {@link Project} is not found.
     */
    public Flux<TaskUser> assignTaskToUsersInProject(Task task, Project project, Map<String, String> statusResponseMap) {
        // find the members of the project
        return projectMembersReactiveRepository.findByProjectId(project.getProjectId())
                .flatMap(projectMembers -> {

                    // find the user associated with the project
                    return reactiveRepositoryUtils.find_UserById_OrElseThrow_ResourceNotFoundException(projectMembers.getUserId())
                            .flatMapMany(user -> {

                                // Check if the task is already assigned to the user, skip ahead and add a response to the map if it is
                                return taskUserReactiveRepository.existsByTaskAndUser(task, user).flatMapMany(taskExists -> {
                                    if (taskExists) {
                                        statusResponseMap.put(user.getUsername(), "Fail: Task already assigned to user {" + user.getUsername() + "}");
                                        return taskUserReactiveRepository.findByTask_TaskId(task.getTaskId());
                                    } else {

                                        // if no task was assigned to user, Assign the task and user to taskUser :: since this class has the responsibility of managing the relationships
                                        TaskUser buildTaskUser = TaskUser.builder()
                                                .task(task)
                                                .user(user)
                                                .build();

                                        statusResponseMap.put(user.getUsername(), "Success");
                                        return taskUserReactiveRepository.save(buildTaskUser);
                                    }
                                });
                            });
                });
    }


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
     * Builds a {@link TaskResponse} object as response for {@link TaskManagementServiceImpl#createNewTask(String, TaskRequest) createNewTask(String projectId, TaskRequest taskRequest)}.
     *
     * @param task the assigned task
     * @return a {@link TaskResponse} object with information about the newly created task
     */
    public Mono<TaskResponse> buildTaskResponse_ForCreatingTask(Task task, Project project) {
        return reactiveRepositoryUtils.find_TaskUsersByTaskId_OrElseThrow_ResourceNotFoundException(task.getTaskId())
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
     * Builds a {@link Task} object as response for {@link TaskManagementServiceImpl#searchTaskBasedOnDifferentCriteria(TaskStatus, TaskPriority, String, String)
     * searchTaskBasedOnDifferentCriteria(TaskStatus taskStatus, TaskPriority taskPriority, String projectId, String assignedTo_UserId)}.
     *
     * @param task {@link Task}.
     * @return Mono of created {@link Task}.
     */
    public Mono<TaskResponse> buildTaskResponse_ForSearchTask(Task task) {
        return reactiveRepositoryUtils.find_TaskUsersByTaskId_OrElseThrow_ResourceNotFoundException(task.getTaskId())
                .collectList().flatMap(taskUsers -> {
                    Map<String, String> assignedUsersMap = new HashMap<>();

                    for (TaskUser taskUser : taskUsers) {
                        String username = taskUser.getUser().getUsername();
                        String userId = taskUser.getUser().getUserId();

                        assignedUsersMap.put(userId, username);
                    }

                    return reactiveRepositoryUtils.find_ProjectById_OrElseThrow_ResourceNotFoundException(task.getProjectId())
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
     * Builds a {@link TaskResponse} object for the given task and project, based on the response map from assigning the task to users.
     * For {@link TaskAssignmentServiceImpl#assignTaskToUsers(String, String, List) assignTaskToUsers(String taskId, String projectId, List&lt;String&gt; userIdList)}.
     * <p>The {@code responseMap} parameter should contain the status messages for each user,
     * where the key is the username and the value is the status message.
     *
     * @param task        the task to build the response for.
     * @param project     the project the task belongs to.
     * @param responseMap a map containing the status of the task assignment for each user.
     * @return a {@link Mono} of {@link TaskResponse} representing the response to the task assignment.
     * @throws ResourceNotFoundException if the task or task users are not found in the database.
     */
    public Mono<TaskResponse> buildTaskResponse_ForAssignTaskToUsers(Task task, Project project, Map<String, String> responseMap) {
        return reactiveRepositoryUtils.find_TaskUsersByTaskId_OrElseThrow_ResourceNotFoundException(task.getTaskId())
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
     * Validates whether the {@link Task} belongs to the {@link Project} and whether the {@link User} is a member and leader/admin of the project.
     *
     * @param task    the task to validate
     * @param project the project to validate against
     * @param user    the user to validate
     * @throws DevVaultException           if the task does not belong to the project
     * @throws NotMemberOfProjectException if the user is not a member of the project
     * @throws NotLeaderOfProjectException if the user is not the leader or admin of the project
     */
    public Mono<Void> validateTaskAndProject(Task task, Project project, User user) {
        // Check if the task belongs to the project or throw a DevVaultException if it doesn't
        return projectTaskReactiveRepository.findByTask_TaskId(task.getTaskId()).flatMap(projectTask ->
                projectReactiveRepository.findByProjectName(projectTask.getProject().getProjectName()).flatMap(foundProject -> {
                    if (!task.getProjectId().equals(foundProject.getProjectId()))
                        return Mono.error(new DevVaultException("Task with ID {" + task.getTaskId() + "} does not belong to project with ID {" + project.getProjectId() + "}"));

                    // Check if the user is a member of the project or throw a NotMemberOfProjectException if they aren't
                    return projectUtils.isMemberOfProject(foundProject, user).flatMap(isMember -> {
                        if (!isMember) {
                            log.info("isMember?: {{}}", isMember);
                            return Mono.error(new NotMemberOfProjectException("You are not a member of this project"));
                        } else {
                            // Check if the user is the leader or admin of the project or throw a NotLeaderOfProjectException if they aren't
                            return projectUtils.isLeaderOrAdminOfProject(foundProject, user).flatMap(isLeader -> {
                                if (!isLeader) {
                                    log.info("isLeader?: {{}}", isLeader);
                                    return Mono.error(new NotLeaderOfProjectException("👮🏻You are not the leader or admin of this project👮🏻"));
                                }
                                return Mono.empty();
                            });
                        }
                    });
                })).then();
    }


}
