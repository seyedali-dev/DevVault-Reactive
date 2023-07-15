package com.dev.vault.model.project;

import com.dev.vault.model.project.enums.JoinStatus;
import com.dev.vault.model.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

/**
 * Entity for sending join project request to a Project - PROJECT_LEADER.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JoinProjectRequest {
    @Id
    private Long joinRequestId;

    /* relationships */
    @Transient
    private Project project;

    @Transient
    private User user;
    /* end of relationships */

    private JoinStatus status;

    public JoinProjectRequest(Project project, User user, JoinStatus status) {
        this.project = project;
        this.user = user;
        this.status = status;
    }
}
