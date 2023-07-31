package com.dev.vault.model.domain.relationship;

import com.dev.vault.model.domain.user.Roles;
import com.dev.vault.model.domain.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Manages the Relationship between {@link User} and {@link Roles}
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class UserRole {

    @Id
    private String userRoleId;

    /* relationships */
    private User user;
    private Roles roles;
    /* end of relationships */

}
