package com.dev.vault.model.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dev.vault.model.user.enums.Permissions.*;

@RequiredArgsConstructor
public enum Roles {
    TEAM_MEMBER(Set.of(TEAM_MEMBER_READ)),
    PROJECT_LEADER(Set.of(
            PROJECT_LEADER_ACCEPT_JOIN_REQUEST, PROJECT_LEADER_DELETE_MEMBER,
            GROUP_ADMIN_ACCEPT_JOIN_REQUEST
    )),
    GROUP_ADMIN(Set.of(
            GROUP_ADMIN_ACCEPT_JOIN_REQUEST
    )),
    ;

    @Getter
    private final Set<Permissions> permissions;

    public List<SimpleGrantedAuthority> authorities() {
        List<SimpleGrantedAuthority> authorities = getPermissions().stream().map(permission ->
                        new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList()
        );
        // we need to also assign the current role
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
