package com.dev.vault.helper.payload.request.project;

import com.dev.vault.model.enums.JoinStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JoinProjectDto {
    private String projectName;
    private Long joinRequestId;
    private String joinRequestUsersEmail;
    private JoinStatus joinStatus;
}
