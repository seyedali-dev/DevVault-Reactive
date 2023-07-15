package com.dev.vault.model.project;

import com.dev.vault.model.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

/**
 * Entity for managing the relationship of members of a Project.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectMembers {
    @Id
    private Long projectMemberId;

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
