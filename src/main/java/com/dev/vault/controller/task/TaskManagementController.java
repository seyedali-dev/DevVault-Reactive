package com.dev.vault.controller.task;

import com.dev.vault.helper.payload.task.TaskRequest;
import com.dev.vault.helper.payload.task.TaskResponse;
import com.dev.vault.service.interfaces.TaskManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for task management.
 */
@RestController
@RequestMapping("/api/v1/task_management")
@RequiredArgsConstructor
public class TaskManagementController {

    private final TaskManagementService taskService;

    /**
     * Creates a new task for the specified project.
     *
     * @param projectId   the ID of the project that the task is being created for
     * @param taskRequest the request object containing the details of the task to create
     * @return a ResponseEntity containing a TaskResponse object and an HTTP status code
     */
    @PreAuthorize("hasAnyRole('PROJECT_LEADER', 'PROJECT_ADMIN')")
    @PostMapping("/newTask/{projectId}")
    public ResponseEntity<TaskResponse> newTask(@Valid @PathVariable Long projectId, @RequestBody TaskRequest taskRequest) {
        return new ResponseEntity<>(taskService.createNewTask(projectId, taskRequest), HttpStatus.CREATED);
    }

    /**
     * Assigns a task to a list of users.
     *
     * @param taskId     the ID of the task to assign
     * @param projectId  the ID of the project the task belongs to
     * @param userIdList the list of user IDs to assign the task to
     * @return a ResponseEntity with an OK HTTP status code and a map of responses for each assigned user
     */
    @PreAuthorize("hasAnyRole('PROJECT_LEADER','PROJECT_ADMIN')")
    @PostMapping("/assignTask")
    public ResponseEntity<?> assignTaskToUsers(
            @RequestParam("taskId") Long taskId,
            @RequestParam("projectId") Long projectId,
            @RequestBody List<Long> userIdList
    ) {
        return ResponseEntity.ok(taskService.assignTaskToUsers(taskId, projectId, userIdList));
    }
}