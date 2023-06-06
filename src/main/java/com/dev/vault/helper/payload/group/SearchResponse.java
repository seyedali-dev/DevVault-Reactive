package com.dev.vault.helper.payload.group;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchResponse {
    private Long projectId;
    private String projectName;
    private String leaderName;
    private String projectDescription;
    private ProjectMembersDto members;
}
