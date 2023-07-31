package com.dev.vault.model.domain.user;

import com.dev.vault.model.domain.relationship.TaskUser;
import com.dev.vault.model.domain.relationship.UserRole;
import com.dev.vault.model.domain.user.jwt.JwtToken;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
@Document
public class User implements UserDetails {

    private ReactiveRepositoryUtils reactiveRepositoryUtils;

    @Id
    private String userId;

    private String username;
    private String password;
    private String email;
    private boolean active = false;
    private int age;
    private String education;
    private String major;

    /* relationships */
    @Transient
    private List<UserRole> userRoles = new ArrayList<>();

    @Transient
    private Set<TaskUser> taskUsers = new HashSet<>();

    @Transient
    private List<JwtToken> jwtTokens;
    /* end of relationships */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        userRoles.forEach(userRole ->
                reactiveRepositoryUtils.find_AllRoleByRoleId_OrElseThrow_ResourceNotFoundException(userRole.getRoles().getRoleId())
                        .map(roles -> roles.getRole().name())
                        .map(SimpleGrantedAuthority::new)
                        .doOnNext(authorities::add)
                        .doOnNext(simpleGrantedAuthority -> log.info("Role added as authority: {}", simpleGrantedAuthority.getAuthority()))
                        .blockLast()
        );
        return authorities;
    }


    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    // todo: implement the security to bypass even if the user is not active, but he/she won't have
    //  any access to any resources accept the login and search groups
    public boolean isEnabled() {
        return this.isActive();
    }
}
