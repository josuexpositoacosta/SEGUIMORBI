package com.seguimorbi.domain.models.caso;

/** Aviso clínico. No es una decisión: la valoración corresponde al médico. */
public record Alerta(TipoAlerta tipo, String mensaje) {
}
