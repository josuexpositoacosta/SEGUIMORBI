package com.seguimorbi.domain.models.vigilancia;

import java.util.Map;

import com.seguimorbi.domain.models.caso.Clasificacion;

/** Casos de una semana epidemiológica (curva epidémica). */
public record ConteoSemana(SemanaEpidemiologica semana, long total, Map<Clasificacion, Long> porClasificacion) {
}
