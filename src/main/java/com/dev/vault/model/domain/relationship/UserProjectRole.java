package com.dev.vault.model.domain.relationship;

import com.dev.vault.model.domain.project.Project;
import com.dev.vault.model.domain.user.Roles;
import com.dev.vault.model.domain.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity for managing the relationship of a `PROJECT_LEADER` with a specific project.
 * This class is used to check if a user is a project leader of a specific project, not other projects.
 * <ul>
 *     <li>
 *         Manages the Relationship between {@link User}, {@link Project} and {@link Roles}
 *     </li>
 * </ul>.
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
    private String userId;
    private String roleId;
    private String projectId;
    /* end of relationships */

    public UserProjectRole(String userId, String roleId, String projectId) {
        this.userId = userId;
        this.roleId = roleId;
        this.projectId = projectId;
    }

}
