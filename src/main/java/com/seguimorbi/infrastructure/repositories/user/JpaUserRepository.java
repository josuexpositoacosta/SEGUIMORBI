
package com.seguimorbi.infrastructure.repositories.user;

import java.util.List;

import com.seguimorbi.domain.models.user.Role;

import com.seguimorbi.infrastructure.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRepository
        extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findAllByOrderByIdAsc();

    long countByRoleAndActivoTrue(Role role);
}
