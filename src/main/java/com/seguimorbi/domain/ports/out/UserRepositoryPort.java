package com.seguimorbi.domain.ports.out;

import java.util.List;
import java.util.Optional;

import com.seguimorbi.domain.models.user.Role;
import com.seguimorbi.domain.models.user.User;

public interface UserRepositoryPort {

    Optional<User> findById(Integer id);

    Optional<User> findByUsername(String username);

    /** Busca por email ya normalizado (ver {@link com.seguimorbi.domain.models.user.Email}). */
    Optional<User> findByEmail(String email);

    User save(User user);

    List<User> findAll();

    long countActivosByRole(Role role);
}
