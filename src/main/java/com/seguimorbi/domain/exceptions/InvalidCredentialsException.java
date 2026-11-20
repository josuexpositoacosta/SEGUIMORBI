package com.seguimorbi.domain.exceptions;

/**
 * Se lanza cuando el usuario o la contraseña no son válidos.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Usuario o contraseña incorrectos");
    }
}
