package com.dev.vault.util.comment;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.NotMemberOfProjectException;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.util.project.ProjectUtilsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility class for managing comments.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CommentUtils {

    private final ProjectUtilsImpl projectUtils;

    /**
     * Validates whether the user is a member and leader/admin of the project.
     *
     * @param project the project to validate against
     * @param user    the user to validate
     * @throws DevVaultException           if the task does not belong to the project
     * @throws NotMemberOfProjectException if the user is not a member of the project
     * @throws NotLeaderOfProjectException if the user is not the leader or admin of the project
     */
    public void validateProject(Project project, User user) {
        // Check if the user is a member of the project or throw a NotMemberOfProjectException if they aren't
        if (!projectUtils.isMemberOfProject(project, user))
            throw new NotMemberOfProjectException("You are not a member of this project");
        // Check if the user is the leader or admin of the project or throw a NotLeaderOfProjectException if they aren't
        if (!projectUtils.isLeaderOrAdminOfProject(project, user))
            throw new NotLeaderOfProjectException("👮🏻You are not the leader or admin of this project👮🏻");
    }
}
