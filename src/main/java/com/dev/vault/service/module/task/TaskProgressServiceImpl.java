package com.dev.vault.service.module.task;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.NotMemberOfProjectException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.model.enums.TaskStatus;
import com.dev.vault.repository.task.TaskReactiveRepository;
import com.dev.vault.service.interfaces.task.TaskProgressService;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import com.dev.vault.util.project.ProjectUtils;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import com.dev.vault.util.task.TaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Service implementation for task progress; marking a task as completed and updating its progress.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskProgressServiceImpl implements TaskProgressService {

    private final TaskReactiveRepository taskRepository;
    private final TaskUtils taskUtils;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;
    private final AuthenticationService authenticationService;
    private final ProjectUtils projectUtils;


    /**
     * Marks a task as completed.
     *
     * @param taskId     the ID of the task to mark as completed
     * @param projectId  the ID of the project to which the task belongs
     * @param taskStatus the status of the task
     * @return a Mono of Void representing the completion of the marking process
     * @throws ResourceNotFoundException   if the task or project is not found
     * @throws NotLeaderOfProjectException if the user is not a leader of the project
     * @throws NotMemberOfProjectException if the user is not a member of the project
     */
    @Override
    public Mono<Void> markTaskAsCompleted(String taskId, String projectId, TaskStatus taskStatus) {
        // 1. find the task, project and the current user
        // 2. Validate that the task belongs to the project and the user is a member or leader/admin of the project
        return reactiveRepositoryUtils.find_ProjectById_OrElseThrow_ResourceNotFoundException(projectId)
                .flatMap(project -> authenticationService.getCurrentUserMono()
                        .flatMap(user -> projectUtils.isMemberOfProject(project, user)
                                .flatMap(isMemberOfProject -> taskUtils.handleUserMembership(isMemberOfProject, project, user))
                                .flatMap(isMemberOfProject -> projectUtils.isLeaderOrAdminOfProject(project, user))
                                .flatMap(isLeaderOrAdminOfProject -> taskUtils.handleUserLeadership(isLeaderOrAdminOfProject, project, user))
                                .flatMap(isLeaderOrAdminOfProject ->
                                        reactiveRepositoryUtils.find_TaskById_OrElseThrow_ResourceNotFoundException(taskId)
                                                .flatMap(task -> {

                                                    // 3. Check if the task has already been completed and throw an exception if it has
                                                    if (task.getTaskStatus().equals(TaskStatus.COMPLETED))
                                                        return Mono.error(new DevVaultException("Task has already been completed"));

                                                    if (taskStatus.equals(TaskStatus.COMPLETED)) {
                                                        // Set the boolean value indicating whether the task was overdue when it was completed
                                                        task.setHasOverdue(task.getDueDate().isBefore(LocalDateTime.now()));
                                                        task.setTaskStatus(taskStatus);
                                                        task.setCompletionDate(LocalDateTime.now());
                                                        return taskRepository.save(task).then();
                                                    } else
                                                        return Mono.error(new DevVaultException("TaskStatus should be only as COMPLETED"));
                                                })
                                )
                        )
                );
    }

}
