package com.seguimorbi.infrastructure.controllers.admin.dto;

import com.seguimorbi.domain.models.user.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearUsuarioRequest {

    private String username;
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String country;
    private Role role;
    private Long areaSaludId;
}
