package com.dev.vault.model.entity.project;

import com.dev.vault.model.entity.user.Roles;
import com.dev.vault.model.entity.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity for managing the relationship of a PROJECT_LEADER with a specific project.
 * This class is used to check if a user is a project leader of a specific project, not other projects.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class UserProjectRole {

    @Id
    private String userProjectRoleId;

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
