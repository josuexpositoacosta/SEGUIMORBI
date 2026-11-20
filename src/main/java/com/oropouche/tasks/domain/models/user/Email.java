package com.oropouche.tasks.domain.models.user;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Reglas del email de un usuario. Java puro.
 * El nombre de usuario no admite "@", así que un identificador de acceso con "@" es siempre un email.
 */
public final class Email {

    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MAX_LONGITUD = 120;

    private Email() {
    }

    /** Indica si el identificador con el que alguien intenta entrar es un email. */
    public static boolean esIdentificadorEmail(String identificador) {
        return identificador != null && identificador.contains("@");
    }

    /** Recorta y pasa a minúsculas: el email no distingue mayúsculas. */
    public static String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean esValido(String email) {
        return email != null && email.length() <= MAX_LONGITUD && FORMATO.matcher(email).matches();
    }
}
