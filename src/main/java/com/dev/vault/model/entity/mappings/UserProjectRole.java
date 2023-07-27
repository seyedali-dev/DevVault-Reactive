package com.dev.vault.model.entity.mappings;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity for managing the mappings of a PROJECT_LEADER with a specific project.
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
