package ar.utn.solicitudes.dobles;

import ar.utn.solicitudes.Repository.RepositoryDeResponsables;
import ar.utn.solicitudes.models.Responsable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositoryDeResponsablesEnMemoria implements RepositoryDeResponsables {
    private final List<Responsable> responsables = new ArrayList<>();

    @Override
    public Optional<Responsable> buscarPorId(String id) {
        return responsables.stream().filter(r -> r.id.equals(id)).findFirst();
    }

    @Override
    public void guardar(Responsable responsable) {
        responsables.removeIf(r -> r.id.equals(responsable.id));
        responsables.add(responsable);
    }

    @Override
    public void incrementarAsignadas(String id) {
        buscarPorId(id).ifPresent(r -> r.cantidadAsignada++);
    }
}
