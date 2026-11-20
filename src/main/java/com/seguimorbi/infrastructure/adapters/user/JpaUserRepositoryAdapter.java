package com.seguimorbi.infrastructure.adapters.user;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.seguimorbi.domain.models.user.Role;
import com.seguimorbi.domain.models.user.User;
import com.seguimorbi.domain.ports.out.UserRepositoryPort;
import com.seguimorbi.infrastructure.entities.UserEntity;
import com.seguimorbi.infrastructure.repositories.user.JpaUserRepository;

@Repository
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaUserRepository;

    public JpaUserRepositoryAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public Optional<User> findById(Integer id) {
        return jpaUserRepository.findById(id).map(UserEntity::toDomainModel);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username).map(UserEntity::toDomainModel);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(UserEntity::toDomainModel);
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(UserEntity.fromDomainModel(user)).toDomainModel();
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAllByOrderByIdAsc().stream()
                .map(UserEntity::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public long countActivosByRole(Role role) {
        return jpaUserRepository.countByRoleAndActivoTrue(role);
    }
}
