package com.dev.vault.helper.payload.auth;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticationResponse {
    private String username;
    private String role;
    private String roleDescription;
    private String token;
}
