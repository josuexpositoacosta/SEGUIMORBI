package com.seguimorbi.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.seguimorbi.application.services.acceso.AtencionClinicaUseCaseConAcceso;
import com.seguimorbi.application.services.acceso.CasoUseCaseConAcceso;
import com.seguimorbi.application.services.acceso.PacienteUseCaseConAcceso;
import com.seguimorbi.application.services.acceso.PoliticaAcceso;
import com.seguimorbi.application.services.acceso.UsuarioPropioConAcceso;
import com.seguimorbi.application.services.acceso.VigilanciaUseCaseConAcceso;
import com.seguimorbi.application.services.vigilancia.VigilanciaService;
import com.seguimorbi.domain.models.user.Email;
import com.seguimorbi.domain.ports.in.VigilanciaUseCase;
import com.seguimorbi.application.services.area_salud.AreaSaludService;
import com.seguimorbi.application.services.user.GestionUsuariosService;
import com.seguimorbi.domain.ports.in.GestionUsuariosUseCase;
import com.seguimorbi.application.services.caso.AtencionClinicaService;
import com.seguimorbi.application.services.caso.CasoService;
import com.seguimorbi.application.services.caso.DesenlaceService;
import com.seguimorbi.domain.ports.in.AtencionClinicaUseCase;
import com.seguimorbi.domain.ports.in.DesenlaceUseCase;
import com.seguimorbi.domain.ports.out.DesenlaceRepositoryPort;
import com.seguimorbi.domain.models.caso.ClasificadorOPS;
import com.seguimorbi.domain.models.caso.DefinicionCaso;
import com.seguimorbi.domain.ports.in.CasoUseCase;
import com.seguimorbi.domain.ports.out.CasoRepositoryPort;
import com.seguimorbi.domain.ports.out.UsuarioActualPort;
import com.seguimorbi.application.services.paciente.PacienteService;
import com.seguimorbi.application.usecases.GetUserUseCaseImpl;
import com.seguimorbi.application.usecases.LoginUseCaseImpl;
import com.seguimorbi.application.usecases.UpdateUserUseCaseImpl;
import com.seguimorbi.domain.ports.in.AreaSaludUseCase;
import com.seguimorbi.domain.ports.in.LoginUseCase;
import com.seguimorbi.domain.ports.in.PacienteUseCase;
import com.seguimorbi.domain.ports.out.AreaSaludRepositoryPort;
import com.seguimorbi.domain.ports.out.AuthenticationPort;
import com.seguimorbi.domain.ports.out.PacienteRepositoryPort;
import com.seguimorbi.domain.ports.out.PasswordEncoderPort;
import com.seguimorbi.domain.ports.out.TokenProviderPort;
import com.seguimorbi.domain.ports.out.UserRepositoryPort;
import com.seguimorbi.infrastructure.security.UserDetailsAdapter;
import com.seguimorbi.infrastructure.transaction.TransactionalUpdateUserUseCase;

/**
 * Composición de la aplicación: conecta los casos de uso (Java puro) con sus adaptadores.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.seguimorbi.infrastructure.repositories")
public class ApplicationConfig {

    // Política de acceso por rol y área (Java puro)

    @Bean
    public PoliticaAcceso politicaAcceso(UsuarioActualPort usuarioActualPort) {
        return new PoliticaAcceso(usuarioActualPort);
    }

    // Áreas de salud y pacientes

    @Bean
    public AreaSaludUseCase areaSaludUseCase(AreaSaludRepositoryPort areaSaludRepositoryPort) {

        return new AreaSaludService(areaSaludRepositoryPort);
    }

    @Bean
    public PacienteUseCase pacienteUseCase(
            PacienteRepositoryPort pacienteRepositoryPort,
            AreaSaludRepositoryPort areaSaludRepositoryPort,
            PoliticaAcceso politicaAcceso) {

        return new PacienteUseCaseConAcceso(
                new PacienteService(pacienteRepositoryPort, areaSaludRepositoryPort),
                pacienteRepositoryPort, politicaAcceso);
    }

    // Casos de Oropouche

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /** Definición de caso vigente. Para usar una definición nacional, sustituir esta bean. */
    @Bean
    public DefinicionCaso definicionCaso() {
        return new ClasificadorOPS();
    }

    @Bean
    public CasoUseCase casoUseCase(
            CasoRepositoryPort casoRepositoryPort,
            PacienteRepositoryPort pacienteRepositoryPort,
            UsuarioActualPort usuarioActualPort,
            DefinicionCaso definicionCaso,
            Clock clock,
            PoliticaAcceso politicaAcceso) {

        return new CasoUseCaseConAcceso(
                new CasoService(casoRepositoryPort, pacienteRepositoryPort, usuarioActualPort, definicionCaso, clock),
                casoRepositoryPort, pacienteRepositoryPort, politicaAcceso);
    }

    @Bean
    public AtencionClinicaUseCase atencionClinicaUseCase(
            CasoRepositoryPort casoRepositoryPort,
            DesenlaceRepositoryPort desenlaceRepositoryPort,
            UsuarioActualPort usuarioActualPort,
            DefinicionCaso definicionCaso,
            Clock clock,
            PoliticaAcceso politicaAcceso) {

        return new AtencionClinicaUseCaseConAcceso(
                new AtencionClinicaService(
                        casoRepositoryPort, desenlaceRepositoryPort, usuarioActualPort, definicionCaso, clock),
                casoRepositoryPort, politicaAcceso);
    }

    @Bean
    public DesenlaceUseCase desenlaceUseCase(DesenlaceRepositoryPort desenlaceRepositoryPort) {

        return new DesenlaceService(desenlaceRepositoryPort);
    }

    // Vigilancia epidemiológica

    @Bean
    public VigilanciaUseCase vigilanciaUseCase(
            CasoRepositoryPort casoRepositoryPort,
            AreaSaludRepositoryPort areaSaludRepositoryPort,
            Clock clock,
            PoliticaAcceso politicaAcceso) {

        return new VigilanciaUseCaseConAcceso(
                new VigilanciaService(casoRepositoryPort, areaSaludRepositoryPort, clock), politicaAcceso);
    }

    // Autenticación y usuarios

    @Bean
    public LoginUseCase loginUseCase(
            AuthenticationPort authenticationPort,
            UserRepositoryPort userRepositoryPort,
            TokenProviderPort tokenProviderPort) {

        return new LoginUseCaseImpl(authenticationPort, userRepositoryPort, tokenProviderPort);
    }

    @Bean
    public GestionUsuariosUseCase gestionUsuariosUseCase(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoderPort passwordEncoderPort,
            AreaSaludRepositoryPort areaSaludRepositoryPort,
            UsuarioActualPort usuarioActualPort) {

        return new GestionUsuariosService(
                userRepositoryPort, passwordEncoderPort, areaSaludRepositoryPort, usuarioActualPort);
    }

    /**
     * Datos personales propios: una sola bean que implementa GetUserUseCase, UpdateUserUseCase y
     * PerfilActualUseCase, con la regla "cada usuario solo los suyos" (salvo el ADMIN).
     */
    @Bean
    public UsuarioPropioConAcceso usuarioPropioUseCase(
            UserRepositoryPort userRepositoryPort,
            PlatformTransactionManager transactionManager,
            PoliticaAcceso politicaAcceso) {

        return new UsuarioPropioConAcceso(
                new GetUserUseCaseImpl(userRepositoryPort),
                new TransactionalUpdateUserUseCase(
                        new UpdateUserUseCaseImpl(userRepositoryPort),
                        new TransactionTemplate(transactionManager)),
                politicaAcceso);
    }

    // Spring Security

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepositoryPort userRepositoryPort) {

        // Se puede entrar con el nombre de usuario o con el email.
        return username -> (Email.esIdentificadorEmail(username)
                        ? userRepositoryPort.findByEmail(Email.normalizar(username))
                        : userRepositoryPort.findByUsername(username))
                .map(UserDetailsAdapter::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found"));
    }
}
