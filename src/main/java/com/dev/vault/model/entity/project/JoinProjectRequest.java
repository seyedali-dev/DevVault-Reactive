package com.dev.vault.model.entity.project;

import com.dev.vault.model.enums.JoinStatus;
import com.dev.vault.model.entity.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity for sending join project request to a Project - PROJECT_LEADER.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class JoinProjectRequest {

    @Id
    private String joinRequestId;

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
