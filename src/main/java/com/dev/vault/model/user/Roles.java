package com.dev.vault.model.user;

import com.dev.vault.model.user.enums.Role;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Roles {
    @Id
    private Long roleId;

    private Role role;

    /* relationships */
    private Set<User> users = new HashSet<>();
    /* end of relationships */
}
