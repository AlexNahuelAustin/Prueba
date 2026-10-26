package ar.utn.solicitudes.Repository;

import ar.utn.solicitudes.models.Responsable;

import java.util.Optional;

public interface RepositoryDeResponsables {

    Optional<Responsable> buscarPorId(String id);

    void guardar(Responsable responsable);

    void incrementarAsignadas(String id);
}
