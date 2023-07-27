package com.dev.vault.util.task;

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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dev.vault.model.enums.TaskStatus.IN_PROGRESS;
import static java.util.stream.Collectors.toSet;


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
     * Builds a TaskResponse object with information about the newly created task.
     *
     * @param task the assigned task
     * @return a TaskResponse object with information about the newly created task
     */
    public TaskResponse buildTaskResponse_ForCreatingTask(Task task, Project project) {
        return TaskResponse.builder()
                .taskName(task.getTaskName())
                .projectName(project.getProjectName())
                .taskStatus(task.getTaskStatus())
                .dueDate(task.getDueDate())
                .assignedUsers(task.getAssignedTaskUser().stream().map(taskUser -> taskUser.getUser().getUsername()).collect(toSet()))
                .taskPriority(task.getTaskPriority())
                .build();
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
        return reactiveRepositoryUtils.findProjectById_OrElseThrow_ResourceNotFoundException(task.getProjectId())
                .flatMap(project ->
                        reactiveRepositoryUtils.findTaskUsersByTaskId_OrElseThrow_ResourceNoFoundException(task.getTaskId())
                                .flatMap(taskUser -> reactiveRepositoryUtils.findUserById_OrElseThrow_ResourceNotFoundException(taskUser.getUser().getUserId()))
                                .collectList()
                                .flatMap(users ->
                                        Mono.just(TaskResponse.builder()
                                                .taskName(task.getTaskName())
                                                .projectName(project.getProjectName())
                                                .taskStatus(task.getTaskStatus())
                                                .dueDate(task.getDueDate())
                                                .taskPriority(task.getTaskPriority())
                                                .assignedUsers(
                                                        users.stream()
                                                                .map(User::getUsername)
                                                                .collect(Collectors.toSet())
                                                )
                                                .build()
                                        )
                                )
                );
    }

    public Mono<TaskResponse> buildTaskResponse(Task task, Project project, Map<String, String> responseMap) {
        return null;
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

//    public List<Task> overDueTasks(Project project) {
//        ArrayList<Task> overDueTasks = new ArrayList<>();
//        for (Task task : project.getTasks()) {
//            if (task.is)
//        }
//    }
}
