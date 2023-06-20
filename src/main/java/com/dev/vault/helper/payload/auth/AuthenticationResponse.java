package com.dev.vault.helper.payload.auth;

import com.dev.vault.helper.payload.user.RoleDescription;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticationResponse {
    private String username;
    private List<String> roles;
    private List<RoleDescription> rolesDescription;
    private String token;

    // constructor, getters, and setters
}
