package com.dev.vault.model.group;

import com.dev.vault.model.user.Roles;
import com.dev.vault.model.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for managing the relationship of a PROJECT_LEADER with a specific project.
 * This class is used to check if a user is a project leader of a specific project, not other projects.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_project_role")
public class UserProjectRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userProjectRoleId;

    /* relationships */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Roles role;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    /* end of relationships */

    public UserProjectRole(User user, Roles role, Project project) {
        this.user = user;
        this.role = role;
        this.project = project;
    }
}
