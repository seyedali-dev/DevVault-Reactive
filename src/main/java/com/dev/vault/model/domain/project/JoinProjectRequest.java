package com.dev.vault.model.domain.project;

import com.dev.vault.model.enums.JoinStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
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
    private String projectId;
    private String userId;
    /* end of relationships */

    private JoinStatus status;

    public JoinProjectRequest(String projectId, String userId, JoinStatus status) {
        this.projectId = projectId;
        this.userId = userId;
        this.status = status;
    }
}
