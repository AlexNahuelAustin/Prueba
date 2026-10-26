package ar.utn.solicitudes.Repository;

import ar.utn.solicitudes.models.Solicitud;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de solicitudes. Lo define el dominio;
 * la infraestructura provee la implementacion concreta.
 */
public interface RepositoryDeSolicitudes {
    int siguienteId();

    void guardar(Solicitud solicitud);

    Optional<Solicitud> buscarPorId(int id);

    List<Solicitud> buscarPorEstado(String estado);

    List<Solicitud> buscarPorTipo(String tipo);

    List<Solicitud> buscarPorResponsable(String idResponsable);

    List<Solicitud> buscarTodas();

    int contarAbiertasDe(String legajo);

    void actualizarEstado(int id, String nuevoEstado, java.time.LocalDate fechaCierre);

    void actualizarResponsable(int id, String idResponsable);
}
