package ar.utn.solicitudes.dobles;

import ar.utn.solicitudes.Repository.RepositoryDeSolicitudes;
import ar.utn.solicitudes.models.Solicitud;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositoryDeSolicitudesEnMemoria implements RepositoryDeSolicitudes {
    private final List<Solicitud> solicitudes = new ArrayList<>();

    @Override
    public int siguienteId() {
        return solicitudes.stream().mapToInt(s -> s.id).max().orElse(0) + 1;
    }

    @Override
    public void guardar(Solicitud solicitud) {
        solicitudes.removeIf(s -> s.id == solicitud.id);
        solicitudes.add(solicitud);
    }

    @Override
    public Optional<Solicitud> buscarPorId(int id) {
        return solicitudes.stream().filter(s -> s.id == id).findFirst();
    }

    @Override
    public List<Solicitud> buscarPorEstado(String estado) {
        return solicitudes.stream().filter(s -> estado.equals(s.estado)).toList();
    }

    @Override
    public List<Solicitud> buscarPorTipo(String tipo) {
        return solicitudes.stream().filter(s -> tipo.equals(s.tipo)).toList();
    }

    @Override
    public List<Solicitud> buscarPorResponsable(String idResponsable) {
        return solicitudes.stream()
                .filter(s -> idResponsable.equals(s.idResponsable))
                .toList();
    }

    @Override
    public List<Solicitud> buscarTodas() {
        return List.copyOf(solicitudes);
    }

    @Override
    public int contarAbiertasDe(String legajo) {
        return (int) solicitudes.stream()
                .filter(s -> legajo.equals(s.legajoEstudiante))
                .filter(s -> s.fechaCierre == null)
                .count();
    }

    @Override
    public void actualizarEstado(int id, String nuevoEstado, LocalDate fechaCierre) {
        buscarPorId(id).ifPresent(s -> {
            s.estado = nuevoEstado;
            s.fechaCierre = fechaCierre;
        });
    }

    @Override
    public void actualizarResponsable(int id, String idResponsable) {
        buscarPorId(id).ifPresent(s -> s.idResponsable = idResponsable);
    }
}
