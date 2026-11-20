package com.seguimorbi.domain.ports.in;

import java.util.Optional;

import com.seguimorbi.domain.models.user.User;

/**
 * Puerto de entrada: consultar un usuario.
 */
public interface GetUserUseCase {

    Optional<User> getUser(Integer id);
}
