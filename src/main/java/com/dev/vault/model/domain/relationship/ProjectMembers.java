package com.dev.vault.model.domain.relationship;

import com.dev.vault.model.domain.project.Project;
import com.dev.vault.model.domain.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Manages the relationship of members of a Project. Relationship between {@link Project} and {@link User}.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class ProjectMembers {

    @Id
    private String projectMemberId;

    /* relationships */
    private String userId;
    private String projectId;
    /* end of relationships */

    public ProjectMembers(String userId, String projectId) {
        this.userId = userId;
        this.projectId = projectId;
    }

}
