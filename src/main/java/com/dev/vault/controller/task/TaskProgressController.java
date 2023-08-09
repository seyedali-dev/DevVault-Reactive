package com.dev.vault.controller.task;

import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.NotMemberOfProjectException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.model.enums.TaskStatus;
import com.dev.vault.service.interfaces.task.TaskProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller for marking tasks as completed.
 * Requires the `PROJECT_LEADER` or `PROJECT_ADMIN` role to access.
 */
@RestController
@RequestMapping("/api/v1/task/progress")
@RequiredArgsConstructor
//@PreAuthorize("hasAnyRole('PROJECT_LEADER','PROJECT_ADMIN')")
public class TaskProgressController {

    private final TaskProgressService taskService;


    /**
     * Marks a task as completed.
     *
     * @param taskId     the ID of the task to mark as completed
     * @param projectId  the ID of the project to which the task belongs
     * @param taskStatus the status of the task
     * @return a Mono of ResponseEntity<Void> representing the completion of the marking process
     * @throws ResourceNotFoundException   if the task or project is not found
     * @throws NotLeaderOfProjectException if the user is not a leader of the project
     * @throws NotMemberOfProjectException if the user is not a member of the project
     */
    @PostMapping
    public Mono<ResponseEntity<Void>> markTaskAsCompleted(
            @RequestParam("taskId") String taskId,
            @RequestParam("projectId") String projectId,
            @RequestParam TaskStatus taskStatus
    ) throws ResourceNotFoundException, NotLeaderOfProjectException, NotMemberOfProjectException {
        return taskService.markTaskAsCompleted(taskId, projectId, taskStatus)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

}
