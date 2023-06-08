package com.dev.vault.model.user;


import com.dev.vault.model.user.enums.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;
    private String password;
    @NotNull
    @NotBlank
    @Column(unique = true)
    private String email;
    private boolean active = false;
    private int age;
    private String education;
    private String major;

    private Roles roles;
    private String rolesDescription;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.authorities();
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

    @Override // todo: implement the security to bypass even if the user is not active, but he/she won't have any
              // any access to any resources accept the login and search groups
    public boolean isEnabled() {
        return this.isActive();
    }
}
