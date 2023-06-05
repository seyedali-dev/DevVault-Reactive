package com.dev.vault.helper.payload.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectMembersDto {
    private List<UserDto> projectMembers;
}
