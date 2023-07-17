package com.dev.vault.model.entity.user;

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
    private Set<User> users = new HashSet<>();
    /* end of relationships */
}
