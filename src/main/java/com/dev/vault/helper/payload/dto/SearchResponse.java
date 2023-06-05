package com.dev.vault.helper.payload.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchResponse {
    private String projectName;
    private String leaderName;
    private String projectDescription;
    private ProjectMembersDto members;
}
