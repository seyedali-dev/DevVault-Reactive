package com.dev.vault.helper.payload.dto;

import com.dev.vault.model.group.ProjectMembers;
import lombok.*;

import java.util.List;

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
