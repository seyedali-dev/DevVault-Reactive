package com.dev.vault.controller.task;

import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.NotMemberOfProjectException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.task.TaskRequest;
import com.dev.vault.helper.payload.response.task.TaskResponse;
import com.dev.vault.model.enums.TaskPriority;
import com.dev.vault.model.enums.TaskStatus;
import com.dev.vault.service.interfaces.task.TaskManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.CREATED;


/**
 * REST controller for task management such as; new task, delete task, update task, search task, get details of task, export tasks.
 */
@RestController
@RequestMapping("/api/v1/task/management")
@RequiredArgsConstructor
//@PreAuthorize("hasAnyRole('PROJECT_LEADER','PROJECT_ADMIN')")
public class TaskManagementController {

    private final TaskManagementService taskService;


    /**
     * Creates a new task for the specified project.
     *
     * @param projectId   the ID of the project that the task is being created for
     * @param taskRequest the request object containing the details of the task to create
     * @return a Mono of ResponseEntity containing a TaskResponse object and an HTTP status code
     */
    @PostMapping("/newTask")
    public Mono<ResponseEntity<TaskResponse>> newTask(@RequestParam String projectId, @Valid @RequestBody TaskRequest taskRequest)
            throws ResourceNotFoundException, ResourceAlreadyExistsException, NotMemberOfProjectException, NotLeaderOfProjectException {
        return taskService.createNewTask(projectId, taskRequest)
                .map(createdTask -> new ResponseEntity<>(createdTask, CREATED));
    }


    /**
     * Searches for tasks based on different criteria.
     *
     * @param status            the status of the tasks to search for
     * @param priority          the priority of the tasks to search for
     * @param projectId         the ID of the project to search for tasks in
     * @param assignedTo_UserId the ID of the user the tasks are assigned to
     * @return a Mono of ResponseEntity containing a Flux of TaskResponse objects and an HTTP status code
     */
    @GetMapping("/searchTasks")
    public Mono<ResponseEntity<Flux<TaskResponse>>> searchTaskBasedOnDifferentCriteria(
            @RequestParam(value = "status", required = false) TaskStatus status,
            @RequestParam(value = "priority", required = false) TaskPriority priority,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "assignedTo_UserId", required = false) String assignedTo_UserId
    ) {
        return Mono.just(
                ResponseEntity.ok(taskService.searchTaskBasedOnDifferentCriteria(status, priority, projectId, assignedTo_UserId))
        );
    }


    /**
     * Updates the details of an existing task.
     *
     * @param taskId      the ID of the task to update.
     * @param taskRequest the request object containing the updated details of the task.
     * @return a <code>Mono&lt;ResponseEntity&gt;</code> containing a {@link TaskResponse} object and an <code>OK</code> {@link HttpStatus} code.
     */
    @PutMapping("/updateTask")
    public Mono<ResponseEntity<TaskResponse>> updateTask(@RequestParam String taskId, @Valid @RequestBody TaskRequest taskRequest)
            throws ResourceNotFoundException {
        return taskService.updateTaskDetails(taskId, taskRequest)
                .map(ResponseEntity::ok);
    }
//
//
//    /**
//     * Retrieves the details of a specific task.
//     *
//     * @param taskId the ID of the task to retrieve
//     * @return a ResponseEntity containing a TaskResponse object and an HTTP status code
//     */
//
//    @SuppressWarnings("CommentedOutCode")
//    @GetMapping("/task/{taskId}") //TODO
//    public ResponseEntity<TaskResponse> getTaskDetails(@PathVariable Long taskId) {
////        TaskResponse taskResponse = taskService.getTaskDetails(taskId);
////        return ResponseEntity.ok(taskResponse);
//        return null;
//    }
//
//
//    /**
//     * Deletes a task by its ID.
//     *
//     * @param taskId the ID of the task to delete
//     * @return a ResponseEntity with an OK HTTP status code
//     */
//    @SuppressWarnings("CommentedOutCode")
//    @DeleteMapping("/deleteTask/{taskId}") //TODO
//    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
////        taskService.deleteTask(taskId);
////        return ResponseEntity.ok().build();
//        return null;
//    }
//
//
//
//
//    /**
//     * Exports tasks to a specified format.
//     *
//     * @param format     the format to export the tasks to (CSV, Excel, PDF, etc.)
//     * @param status     the status of the tasks to export
//     * @param priority   the priority of the tasks to export
//     * @param projectId  the ID of the project to export tasks for
//     * @param assignedTo the ID of the user the tasks are assigned to
//     * @return a ResponseEntity containing the exported file and an HTTP status code
//     */
//    @GetMapping("/exportTasks") //TODO
//    public ResponseEntity<?> exportTasks(
//            @RequestParam(value = "format") String format,
//            @RequestParam(value = "status", required = false) TaskStatus status,
//            @RequestParam(value = "priority", required = false) TaskPriority priority,
//            @RequestParam(value = "projectId", required = false) Long projectId,
//            @RequestParam(value = "assignedTo", required = false) Long assignedTo
//    ) {
//        return null;
//    }

}
