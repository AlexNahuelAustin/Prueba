package ar.utn.solicitudes.services;

import ar.utn.solicitudes.Repository.RepositoryDeEstudiantes;
import ar.utn.solicitudes.Repository.RepositoryDeResponsables;
import ar.utn.solicitudes.Repository.RepositoryDeSolicitudes;
import ar.utn.solicitudes.models.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio del modulo de solicitudes.
 * No conoce la tecnologia de persistencia: trabaja contra los puertos.
 */
public class ServicioDeSolicitudes {
    private static final int MAXIMO_SOLICITUDES_ABIERTAS_POR_ESTUDIANTE = 5;
    private static final int MAXIMO_ASIGNACIONES_POR_RESPONSABLE = 10;

    private final RepositoryDeSolicitudes solicitudes;
    private final RepositoryDeEstudiantes estudiantes;
    private final RepositoryDeResponsables responsables;

    public ServicioDeSolicitudes(RepositoryDeSolicitudes solicitudes,
                                 RepositoryDeEstudiantes estudiantes,
                                 RepositoryDeResponsables responsables) {
        this.solicitudes = solicitudes;
        this.estudiantes = estudiantes;
        this.responsables = responsables;
    }
    public Optional<Solicitud> buscarPorId(int id) {
        return solicitudes.buscarPorId(id);
    }

    public int registrarSolicitud(DatosDeEstudiante datos, String tipo, String obs) {

        if (datos.legajo() == null || datos.legajo().isBlank()) {
            System.out.println("ERROR: falta el legajo");
            return -1;
        }
        if (datos.email() == null || !datos.email().contains("@")) {
            System.out.println("ERROR: email invalido");
            return -1;
        }
        TipoDeSolicitud tipoDeSolicitud = TipoDeSolicitud.desdeCodigo(tipo);
        if (tipoDeSolicitud == null) {
            System.out.println("ERROR: tipo invalido");
            return -1;
        }

        if (!estudiantes.existe(datos.legajo())) {
            estudiantes.guardar(datos);
        }

        if (solicitudes.contarAbiertasDe(datos.legajo())
                >= MAXIMO_SOLICITUDES_ABIERTAS_POR_ESTUDIANTE) {
            System.out.println("ERROR: el estudiante tiene demasiadas solicitudes abiertas");
            return -1;
        }

        Solicitud nueva = new Solicitud(solicitudes.siguienteId(), datos.legajo(), tipo);
        nueva.prioridad = tipoDeSolicitud.prioridad();
        nueva.observaciones = obs;
        solicitudes.guardar(nueva);

        System.out.println("Solicitud " + nueva.id + " registrada correctamente");
        return nueva.id;
    }

    public boolean cambiarEstado(int id, String nuevoEstado) {

        Optional<Solicitud> encontrada = solicitudes.buscarPorId(id);
        if (encontrada.isEmpty()) {
            System.out.println("ERROR: no existe la solicitud");
            return false;
        }
        Solicitud solicitud = encontrada.get();
        EstadoSolicitud actual = EstadoSolicitud.desdeCodigo(solicitud.estado);
        EstadoSolicitud destino = EstadoSolicitud.desdeCodigo(nuevoEstado);

        if (actual.esFinal()) {
            System.out.println("ERROR: la solicitud ya esta cerrada");
            return false;
        }
        if (destino == null || !esTransicionValida(actual, destino)) {
            System.out.println("ERROR: transicion invalida");
            return false;
        }
        if (destino == EstadoSolicitud.EN_PROCESO && solicitud.idResponsable == null) {
            System.out.println("ERROR: no se puede procesar sin responsable");
            return false;
        }

        LocalDate fechaCierre = destino.esFinal() ? LocalDate.now() : null;
        solicitudes.actualizarEstado(id, destino.codigo(), fechaCierre);

        System.out.println("Solicitud " + id + " paso a " + nuevoEstado);
        return true;
    }

    private boolean esTransicionValida(EstadoSolicitud actual, EstadoSolicitud destino) {
        return switch (actual) {
            case PENDIENTE -> destino == EstadoSolicitud.EN_PROCESO
                    || destino == EstadoSolicitud.RECHAZADA;
            case EN_PROCESO -> destino == EstadoSolicitud.RESUELTA
                    || destino == EstadoSolicitud.RECHAZADA;
            case RESUELTA, RECHAZADA -> false;
        };
    }

    public boolean asignarResponsable(int id, String idResponsable) {

        if (solicitudes.buscarPorId(id).isEmpty()) {
            System.out.println("ERROR: no existe la solicitud");
            return false;
        }

        Optional<Responsable> encontrado = responsables.buscarPorId(idResponsable);
        if (encontrado.isEmpty()) {
            System.out.println("ERROR: no existe el responsable");
            return false;
        }
        Responsable responsable = encontrado.get();

        if (responsable.cantidadAsignada >= MAXIMO_ASIGNACIONES_POR_RESPONSABLE) {
            System.out.println("ERROR: el responsable esta sobrecargado");
            return false;
        }

        solicitudes.actualizarResponsable(id, idResponsable);
        responsables.incrementarAsignadas(idResponsable);

        System.out.println("Solicitud " + id + " asignada a " + responsable.nombre);
        return true;
    }

    public void altaResponsable(Responsable responsable) {
        responsables.guardar(responsable);
    }

    public List<Solicitud> buscarPorEstado(String estado) {
        return solicitudes.buscarPorEstado(estado);
    }

    public List<Solicitud> buscarPorTipo(String tipo) {
        return solicitudes.buscarPorTipo(tipo);
    }

    public List<Solicitud> buscarPorResponsable(String idResponsable) {
        return solicitudes.buscarPorResponsable(idResponsable);
    }

    public List<Solicitud> buscarTodas() {
        return solicitudes.buscarTodas();
    }


}
