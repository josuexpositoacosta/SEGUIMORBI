package com.seguimorbi.domain.ports.in;

import java.util.List;

import com.seguimorbi.domain.models.user.Role;
import com.seguimorbi.domain.models.user.User;

/**
 * Puerto de entrada: gestión de usuarios por el administrador.
 */
public interface GestionUsuariosUseCase {

    /** Crea un usuario. {@code nuevo.password} viene en texto plano. */
    User crear(User nuevo);

    List<User> listar();

    /** Cambia rol, área y estado. No permite dejar el sistema sin administradores activos. */
    User actualizar(Integer id, Role role, Long areaSaludId, boolean activo);

    void restablecerPassword(Integer id, String nuevaPassword);

    /** Asigna o cambia el email. Vacío o null lo quita. Debe ser único. */
    User cambiarEmail(Integer id, String email);

    /** Crea el primer administrador si no existe ninguno activo. Devuelve true si lo creó. */
    boolean crearAdminInicialSiNoExiste(String username, String password);
}
