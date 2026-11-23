package com.seguimorbi.application.services.acceso;

import java.util.List;

import com.seguimorbi.domain.models.caso.Caso;
import com.seguimorbi.domain.models.user.Role;
import com.seguimorbi.domain.models.vigilancia.ConteoArea;
import com.seguimorbi.domain.models.vigilancia.ConteoSemana;
import com.seguimorbi.domain.models.vigilancia.FiltroVigilancia;
import com.seguimorbi.domain.models.vigilancia.ResumenVigilancia;
import com.seguimorbi.domain.ports.in.VigilanciaUseCase;

/** Las estadísticas de vigilancia son del epidemiólogo. */
public class VigilanciaUseCaseConAcceso implements VigilanciaUseCase {

    private final VigilanciaUseCase delegado;
    private final PoliticaAcceso politica;

    public VigilanciaUseCaseConAcceso(VigilanciaUseCase delegado, PoliticaAcceso politica) {
        this.delegado = delegado;
        this.politica = politica;
    }

    @Override
    public ResumenVigilancia resumen(FiltroVigilancia filtro) {
        politica.verificarRol(Role.EPIDEMIOLOGO);
        return delegado.resumen(filtro);
    }

    @Override
    public List<ConteoSemana> curvaEpidemica(FiltroVigilancia filtro) {
        politica.verificarRol(Role.EPIDEMIOLOGO);
        return delegado.curvaEpidemica(filtro);
    }

    @Override
    public List<ConteoArea> porArea(FiltroVigilancia filtro) {
        politica.verificarRol(Role.EPIDEMIOLOGO);
        return delegado.porArea(filtro);
    }

    @Override
    public List<Caso> lineaDeCasos(FiltroVigilancia filtro) {
        politica.verificarRol(Role.EPIDEMIOLOGO);
        return delegado.lineaDeCasos(filtro);
    }

    @Override
    public FiltroVigilancia normalizar(FiltroVigilancia filtro) {
        return delegado.normalizar(filtro);
    }
}
