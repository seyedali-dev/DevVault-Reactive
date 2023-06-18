package com.dev.vault.model.user;

import com.dev.vault.model.user.enums.Permissions;
import com.dev.vault.model.user.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ElementCollection(targetClass = Permissions.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "roles_permissions",
            joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "permissions")
    @Enumerated(EnumType.STRING)
    private Set<Permissions> permissions = new HashSet<>();

    @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    private Set<User> users = new HashSet<>();

    public Roles(Role role, Set<Permissions> permissions) {
        this.role = role;
        this.permissions = permissions;
    }
}
