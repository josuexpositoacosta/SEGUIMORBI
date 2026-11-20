package com.seguimorbi.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.seguimorbi.domain.models.user.Role;
import com.seguimorbi.domain.ports.in.DesenlaceUseCase;
import com.seguimorbi.domain.ports.in.GestionUsuariosUseCase;

/**
 * Datos iniciales al arrancar: catálogo de desenlaces y primer administrador (solo si no existen).
 */
@Configuration
public class DatosInicialesConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatosInicialesConfig.class);

    @Bean
    public ApplicationRunner inicializarDatos(
            DesenlaceUseCase desenlaceUseCase,
            GestionUsuariosUseCase gestionUsuarios,
            @Value("${seguimorbi.admin.username:}") String adminUsername,
            @Value("${seguimorbi.admin.password:}") String adminPassword) {

        return args -> {
            desenlaceUseCase.inicializarSiVacio();

            if (gestionUsuarios.crearAdminInicialSiNoExiste(adminUsername, adminPassword)) {
                LOGGER.info("Administrador inicial creado: {}", adminUsername.trim());
            } else if (gestionUsuarios.listar().stream().noneMatch(u -> u.isActivo()
                    && u.getRole() == Role.ADMIN)) {
                LOGGER.warn("No hay ningún administrador activo. Defina ADMIN_USERNAME y ADMIN_PASSWORD"
                        + " (mínimo 8 caracteres) para crearlo al arrancar.");
            }
        };
    }
}
