package com.dev.vault.model.project;

import com.dev.vault.model.user.Roles;
import com.dev.vault.model.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

/**
 * Entity for managing the relationship of a PROJECT_LEADER with a specific project.
 * This class is used to check if a user is a project leader of a specific project, not other projects.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProjectRole {
    @Id
    private Long userProjectRoleId;

    /* relationships */
    @Transient
    private User user;

    @Transient
    private Roles role;

    @Transient
    private Project project;
    /* end of relationships */

    public UserProjectRole(User user, Roles role, Project project) {
        this.user = user;
        this.role = role;
        this.project = project;
    }
}
