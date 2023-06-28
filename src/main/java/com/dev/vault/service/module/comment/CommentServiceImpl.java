package com.dev.vault.service.module.comment;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.NotMemberOfProjectException;
import com.dev.vault.model.comment.Comment;
import com.dev.vault.model.project.Project;
import com.dev.vault.model.task.Task;
import com.dev.vault.model.user.User;
import com.dev.vault.repository.comment.CommentRepository;
import com.dev.vault.service.interfaces.comment.CommentService;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import com.dev.vault.util.comment.CommentUtils;
import com.dev.vault.util.repository.RepositoryUtils;
import com.dev.vault.util.task.TaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing comments on projects and tasks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final RepositoryUtils repositoryUtils;
    private final CommentUtils commentUtils;
    private final AuthenticationService authenticationService;
    private final TaskUtils taskUtils;

    /**
     * Adds a comment to the specified project.
     *
     * @param projectId the ID of the project to comment on
     * @param comment   the comment to add
     * @throws DevVaultException           if the project does not exist
     * @throws NotMemberOfProjectException if the user is not a member of the project
     * @throws NotLeaderOfProjectException if the user is not the leader or admin of the project
     */
    @Override
    @Transactional
    public void commentOnProject(Long projectId, String comment) {
        Project project = repositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(projectId);
        User currentUser = authenticationService.getCurrentUser();
        // Validate that the user is a member and leader/admin of the project
        commentUtils.validateProject(project, currentUser);

        Comment commentMade = new Comment();
        commentMade.setComment(comment);
        commentMade.setCommentedBy(currentUser);
        commentMade.setCommentedOnProject(project);

        commentRepository.save(commentMade);
    }

    /**
     * Adds a comment to the specified task within a project.
     *
     * @param projectId the ID of the project containing the task
     * @param taskId    the ID of the task to comment on
     * @param comment   the comment to add
     * @throws DevVaultException           if the task or project does not exist
     * @throws NotMemberOfProjectException if the user is not a member of the project
     * @throws NotLeaderOfProjectException if the user is not the leader or admin of the project
     */
    @Override
    public void commentOnTask(Long projectId, Long taskId, String comment) {
        Task task = repositoryUtils.findTaskById_OrElseThrow_ResourceNotFoundException(taskId);
        Project project = repositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(projectId);
        User currentUser = authenticationService.getCurrentUser();
        // Validate that the user is a member and leader/admin of the project
        taskUtils.validateTaskAndProject(task, project, currentUser);

        Comment commentMade = new Comment();
        commentMade.setComment(comment);
        commentMade.setCommentedBy(currentUser);
        commentMade.setCommentedOnTask(task);

        commentRepository.save(commentMade);
    }
}
