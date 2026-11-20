package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import java.util.List;

import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.ResultadoClasificacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Vista previa de la clasificación: sugerida (null si no cumple la definición) y criterios evaluados. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SugerenciaResponse {

    private Clasificacion clasificacion;
    private List<String> criterios;

    public static SugerenciaResponse from(ResultadoClasificacion r) {
        return new SugerenciaResponse(r.sugerida().orElse(null), r.criterios());
    }
}
