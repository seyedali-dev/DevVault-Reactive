package com.dev.vault.model.entity.user;

import com.dev.vault.model.entity.task.Task;
import com.dev.vault.model.entity.user.jwt.JwtToken;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class User implements UserDetails {

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
    private Set<Roles> roles = new HashSet<>();

    @Transient
    private List<Task> task = new ArrayList<>();

    @Transient
    private List<JwtToken> jwtTokens;
    /* end of relationships */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Roles eachRole : roles) {
            authorities.add(new SimpleGrantedAuthority(eachRole.getRole().name()));
        }
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
