package com.seguimorbi.infrastructure.adapters.paciente;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.seguimorbi.domain.models.paciente.Paciente;
import com.seguimorbi.domain.ports.out.PacienteRepositoryPort;
import com.seguimorbi.infrastructure.entities.PacienteEntity;
import com.seguimorbi.infrastructure.repositories.paciente.JpaPacienteRepository;

/**
 * Adaptador de salida: persiste pacientes con Spring Data JPA.
 */
@Repository
public class JpaPacienteRepositoryAdapter implements PacienteRepositoryPort {

    private final JpaPacienteRepository repository;

    public JpaPacienteRepositoryAdapter(JpaPacienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Paciente save(Paciente paciente) {
        return repository.save(PacienteEntity.fromDomainModel(paciente)).toDomainModel();
    }

    @Override
    public Optional<Paciente> findById(Long id) {
        return repository.findById(id).map(PacienteEntity::toDomainModel);
    }

    @Override
    public Optional<Paciente> findByNumeroDocumento(String tipoDocumento, String numeroDocumento) {
        return repository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento)
                .map(PacienteEntity::toDomainModel);
    }

    @Override
    public List<Paciente> findByAreaSaludId(Long areaSaludId) {
        return repository.findByAreaSaludId(areaSaludId).stream()
                .map(PacienteEntity::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Paciente> findAll() {
        return repository.findAll().stream()
                .map(PacienteEntity::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNumeroDocumento(String tipoDocumento, String numeroDocumento) {
        return repository.existsByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento);
    }
}
