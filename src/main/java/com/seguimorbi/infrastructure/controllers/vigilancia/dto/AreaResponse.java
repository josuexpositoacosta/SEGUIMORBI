package com.seguimorbi.infrastructure.controllers.vigilancia.dto;

import java.util.Map;

import com.seguimorbi.domain.models.caso.Clasificacion;
import com.seguimorbi.domain.models.vigilancia.ConteoArea;

public record AreaResponse(
        Long areaSaludId,
        String codigo,
        String nombre,
        long total,
        long abiertos,
        Map<Clasificacion, Long> porClasificacion) {

    public static AreaResponse from(ConteoArea c) {
        return new AreaResponse(c.areaSaludId(), c.codigo(), c.nombre(), c.total(), c.abiertos(), c.porClasificacion());
    }
}
