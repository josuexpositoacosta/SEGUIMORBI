package com.seguimorbi.domain.ports.in;

import com.seguimorbi.domain.models.user.User;

/** Puerto de entrada: datos del usuario autenticado (para que el frontend conozca su rol y área). */
public interface PerfilActualUseCase {

    User perfilActual();
}
