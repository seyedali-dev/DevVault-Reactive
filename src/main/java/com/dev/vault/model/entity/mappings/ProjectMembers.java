package com.dev.vault.model.entity.mappings;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity for managing the mappings of members of a Project.
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
