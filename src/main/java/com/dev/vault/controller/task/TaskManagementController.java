package com.dev.vault.controller.task;

import com.dev.vault.helper.payload.task.TaskRequest;
import com.dev.vault.helper.payload.task.TaskResponse;
import com.dev.vault.service.TaskManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for task management.
 */
@RestController
@RequestMapping("/api/v1/task_management")
@RequiredArgsConstructor
public class TaskManagementController {

    private final TaskManagementService taskService;

    /**
     * Create a new task for specified project.
     *
     * @param projectId   the ID of the project that the task is being created for.
     * @param taskRequest the information of task for creation like name and dueDate etc.
     * @return ResponseEntity containing ...
     */
    @PreAuthorize("hasAnyRole('PROJECT_LEADER', 'PROJECT_AMIN')")
    @PostMapping("/newTask/{projectId}")
    public ResponseEntity<TaskResponse> newTask(@PathVariable Long projectId, @RequestBody TaskRequest taskRequest) {
        return new ResponseEntity<>(taskService.createNewTask(projectId, taskRequest), HttpStatus.CREATED);
    }
}
