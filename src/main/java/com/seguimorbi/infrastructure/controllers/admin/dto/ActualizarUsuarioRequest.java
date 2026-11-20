package com.seguimorbi.infrastructure.controllers.admin.dto;

import com.seguimorbi.domain.models.user.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarUsuarioRequest {

    private Role role;
    private Long areaSaludId;
    private boolean activo;
}
