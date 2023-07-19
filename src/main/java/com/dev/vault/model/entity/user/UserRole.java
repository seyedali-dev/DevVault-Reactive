package com.dev.vault.model.entity.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
