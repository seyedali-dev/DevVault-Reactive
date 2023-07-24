package com.dev.vault.helper.payload.request.project;

import com.dev.vault.helper.payload.request.user.UserDto;
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
