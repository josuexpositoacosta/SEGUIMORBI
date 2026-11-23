package com.seguimorbi.domain.ports.out;

/**
 * Puerto de salida: cifrado de contraseñas.
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);
}
