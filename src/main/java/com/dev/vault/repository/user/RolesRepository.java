package com.dev.vault.repository.user;

import com.dev.vault.model.user.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepository extends JpaRepository<Roles, Long> {
}