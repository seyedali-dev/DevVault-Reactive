package com.dev.vault.helper.payload.group;

import com.dev.vault.helper.payload.user.UserDto;
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
