package com.seguimorbi.domain.ports.out;

/**
 * Puerto de salida: verificación de credenciales.
 */
public interface AuthenticationPort {

    /**
     * @throws com.seguimorbi.domain.exceptions.InvalidCredentialsException si no son válidas
     */
    void authenticate(String username, String password);
}
