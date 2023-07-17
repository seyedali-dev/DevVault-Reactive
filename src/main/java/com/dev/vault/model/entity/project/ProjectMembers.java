package com.dev.vault.model.entity.project;

import com.dev.vault.model.entity.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity for managing the relationship of members of a Project.
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
    @Transient
    private User user;

    @Transient
    private Project project;
    /* end of relationships */

    public ProjectMembers(User user, Project project) {
        this.user = user;
        this.project = project;
    }
}
