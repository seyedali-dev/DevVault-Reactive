package com.dev.vault.model.group;

import com.dev.vault.model.group.enums.JoinStatus;
import com.dev.vault.model.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for sending join project request to a Project - PROJECT_LEADER.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class JoinProjectRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long joinRequestId;

    /* relationships */
    @ManyToOne(fetch = FetchType.EAGER)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;
    /* end of relationships */

    private JoinStatus status;

    public JoinProjectRequest(Project project, User user, JoinStatus status) {
        this.project = project;
        this.user = user;
        this.status = status;
    }
}
