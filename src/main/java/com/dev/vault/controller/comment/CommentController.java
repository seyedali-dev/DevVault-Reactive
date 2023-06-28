package com.dev.vault.controller.comment;

import com.dev.vault.helper.payload.dto.ApiResponse;
import com.dev.vault.service.interfaces.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for commenting on a project or in a task.
 */
@RestController
@RequestMapping("/api/v1/comment")
@RequiredArgsConstructor
// This is the controller class for handling comments
public class CommentController {

    private final CommentService commentService;

    /**
     * Handles a POST request to create a comment on a project.
     *
     * @param projectId The ID of the project to comment on.
     * @param comment   The text of the comment.
     * @return A ResponseEntity with a success message and HTTP status code 201 (CREATED).
     */
    @PostMapping("/project/{projectId}")
    public ResponseEntity<?> commentOnProject(@PathVariable Long projectId, @RequestBody String comment) {
        commentService.commentOnProject(projectId, comment);
        return new ResponseEntity<>(new ApiResponse("You commented on projectID: " + projectId, true), HttpStatus.CREATED);
    }

    /**
     * Handles a POST request to create a comment on a task within a project.
     *
     * @param projectId The ID of the project containing the task to comment on.
     * @param taskId    The ID of the task to comment on.
     * @param comment   The text of the comment.
     * @return A ResponseEntity with a success message and HTTP status code 201 (CREATED).
     */
    @PostMapping("/project/{projectId}/task/{taskId}")
    public ResponseEntity<?> commentOnTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody String comment
    ) {
        commentService.commentOnTask(projectId, taskId, comment);
        return new ResponseEntity<>(
                new ApiResponse("You commented on taskID: " + taskId + " in projectID: " + projectId, true),
                HttpStatus.CREATED
        );
    }
}