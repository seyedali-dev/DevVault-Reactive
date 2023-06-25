package com.dev.vault.util.task;

import com.dev.vault.helper.payload.task.TaskRequest;
import com.dev.vault.helper.payload.task.TaskResponse;
import com.dev.vault.model.group.Project;
import com.dev.vault.model.task.Task;
import com.dev.vault.model.user.User;
import com.dev.vault.repository.task.TaskRepository;
import com.dev.vault.repository.user.UserRepository;
import com.dev.vault.util.project.ProjectUtils;
import com.dev.vault.util.repository.RepositoryUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A utility class that provides helper methods for working with tasks.
 * This class contains method for checking if a task with the same name already exists in a project.
 */
@Service
@RequiredArgsConstructor
public class TaskUtils {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectUtils projectUtils;
    private final RepositoryUtils repositoryUtils;

    /**
     * Checks if a task with the same name already exists in the project
     *
     * @param taskRequest the request object containing the details of the task to create
     * @param project     the project to check for existing tasks
     * @return true if a task with the same name already exists in the project, false otherwise
     */
    public boolean doesTaskAlreadyExists(TaskRequest taskRequest, Project project) {
        Optional<Task> foundTask = taskRepository.findByProjectAndTaskName(project, taskRequest.getTaskName());
        return foundTask.isPresent();
    }

    /**
     * Loops through the list of user IDs and assigns the task to each user.
     *
     * @param projectId      the ID of the project the task belongs to
     * @param userIdList     the list of user IDs to assign the task to
     * @param task           the task to assign
     * @param project        the project the task belongs to
     * @param assignedUsers  the set to hold the assigned users
     * @param assignUsersMap the map to hold the responses for each user
     */
    public void assignTaskToUserList(Long projectId, List<Long> userIdList, Task task, Project project, Set<User> assignedUsers, Map<String, String> assignUsersMap) {
        for (Long userId : userIdList) {
            // Find the user by ID or throw a RecourseNotFoundException if it doesn't exist
            User user = repositoryUtils.findUserByIdOrElseThrowNoFoundException(userId);

            // Check if the task is already assigned to the user, and add a response to the map if it is
            if (taskRepository.findByAssignedUsersAndTaskId(user, task.getTaskId()).isPresent()) {
                assignUsersMap.put(user.getUsername(), "Fail: Task already assigned to user " + user.getUsername());
                continue;
            }

            // Check if the user is a member of the project, and add a response to the map if they're not
            if (!projectUtils.isMemberOfProject(project, user)) {
                assignUsersMap.put(user.getUsername(), "Fail: User with ID " + userId + " is not a member of project with ID " + projectId);
                continue;
            }

            // Assign the task to the user, add the user to the assigned users set, and add a response to the map
            user.getTask().add(task);
            User savedUser = userRepository.save(user);
            assignedUsers.add(savedUser);
            assignUsersMap.put(savedUser.getUsername(), "Success: Task assigned to user " + savedUser.getUsername());

            // Set the assigned users for the task and save the task
            task.setAssignedUsers(assignedUsers);
            taskRepository.save(task);
        }
    }

    /**
     * Builds a TaskResponse object with information about the assigned task and its assigned users.
     *
     * @param task    the assigned task
     * @param project the project the task belongs to
     * @param map     the map of responses for each assigned user
     * @return a TaskResponse object with information about the assigned task and its assigned users
     */
    public TaskResponse buildTaskResponse(Task task, Project project, Map<String, String> map) {
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setTaskName(task.getTaskName());
        taskResponse.setTaskStatus(task.getTaskStatus());
        taskResponse.setDueDate(task.getDueDate());
        taskResponse.setProjectName(project.getProjectName());
        taskResponse.setAssignedUsers(map);
        return taskResponse;
    }

    /**
     * Builds a TaskResponse object with information about the newly created task.
     *
     * @param task the assigned task
     * @return a TaskResponse object with information about the newly created task
     */
    public TaskResponse buildTaskResponse(Task task) {
        return TaskResponse.builder()
                .taskName(task.getTaskName())
                .projectName(task.getProject().getProjectName())
                .taskStatus(task.getTaskStatus())
                .dueDate(task.getDueDate())
                .build();
    }
}
