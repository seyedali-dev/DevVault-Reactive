package com.dev.vault.model.domain.user;

import com.dev.vault.model.domain.relationship.UserRole;
import com.dev.vault.model.enums.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class Roles {

    @Id
    private String roleId;

    private Role role;

    /* relationships */
    @Transient
    private Set<UserRole> userRoles = new HashSet<>();
    /* end of relationships */
}
