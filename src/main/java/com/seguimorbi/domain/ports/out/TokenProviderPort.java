package com.seguimorbi.domain.ports.out;

import com.seguimorbi.domain.models.user.User;

/**
 * Puerto de salida: emisión de tokens de acceso.
 */
public interface TokenProviderPort {

    String generateToken(User user);
}
