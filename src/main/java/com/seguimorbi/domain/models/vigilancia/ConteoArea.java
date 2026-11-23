package com.seguimorbi.domain.models.vigilancia;

import java.util.Map;

import com.seguimorbi.domain.models.caso.Clasificacion;

/** Casos de un área de salud en el periodo. */
public record ConteoArea(
        Long areaSaludId,
        String codigo,
        String nombre,
        long total,
        long abiertos,
        Map<Clasificacion, Long> porClasificacion) {
}
