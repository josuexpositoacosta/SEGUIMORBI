package com.seguimorbi.application.usecases;

import java.util.Optional;

import com.seguimorbi.domain.models.user.User;
import com.seguimorbi.domain.ports.in.GetUserUseCase;
import com.seguimorbi.domain.ports.out.UserRepositoryPort;

public class GetUserUseCaseImpl implements GetUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public GetUserUseCaseImpl(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Optional<User> getUser(Integer id) {

        if (id == null) {
            return Optional.empty();
        }

        return userRepositoryPort.findById(id);
    }
}
