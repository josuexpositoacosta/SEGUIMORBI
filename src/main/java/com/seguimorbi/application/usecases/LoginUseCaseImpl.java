package com.seguimorbi.application.usecases;

import java.util.Optional;

import com.seguimorbi.domain.exceptions.InvalidCredentialsException;
import com.seguimorbi.domain.models.user.Email;
import com.seguimorbi.domain.models.user.User;
import com.seguimorbi.domain.ports.in.LoginUseCase;
import com.seguimorbi.domain.ports.out.AuthenticationPort;
import com.seguimorbi.domain.ports.out.TokenProviderPort;
import com.seguimorbi.domain.ports.out.UserRepositoryPort;

public class LoginUseCaseImpl implements LoginUseCase {

    private final AuthenticationPort authenticationPort;
    private final UserRepositoryPort userRepositoryPort;
    private final TokenProviderPort tokenProviderPort;

    public LoginUseCaseImpl(
            AuthenticationPort authenticationPort,
            UserRepositoryPort userRepositoryPort,
            TokenProviderPort tokenProviderPort) {

        this.authenticationPort = authenticationPort;
        this.userRepositoryPort = userRepositoryPort;
        this.tokenProviderPort = tokenProviderPort;
    }

    @Override
    public String login(String username, String password) {

        if (isBlank(username) || isBlank(password)) {
            throw new InvalidCredentialsException();
        }

        authenticationPort.authenticate(username, password);

        User user = buscarPorUsuarioOEmail(username)
                .orElseThrow(InvalidCredentialsException::new);

        return tokenProviderPort.generateToken(user);
    }

    /** El identificador puede ser el nombre de usuario o el email. */
    private Optional<User> buscarPorUsuarioOEmail(String identificador) {
        return Email.esIdentificadorEmail(identificador)
                ? userRepositoryPort.findByEmail(Email.normalizar(identificador))
                : userRepositoryPort.findByUsername(identificador);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
