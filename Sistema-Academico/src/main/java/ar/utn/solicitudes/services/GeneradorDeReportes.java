package ar.utn.solicitudes.services;

import ar.utn.solicitudes.models.EstadoSolicitud;
import ar.utn.solicitudes.models.Solicitud;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Responsabilidad unica: presentar el reporte.
 * Antes estaba dentro del gestor, mezclado con la logica de negocio
 * y el acceso a la base de datos.
 */
public class GeneradorDeReportes {

    private static final int DIAS_PARA_CONSIDERAR_VENCIDA = 30;

    private final ServicioDeSolicitudes servicio;

    public GeneradorDeReportes(ServicioDeSolicitudes servicio) {
        this.servicio = servicio;
    }

    public void imprimir() {
        List<Solicitud> todas = servicio.buscarTodas();

        System.out.println("========================================");
        System.out.println("   REPORTE DE SOLICITUDES ACADEMICAS");
        System.out.println("========================================");

        System.out.println("Pendientes:  " + contarPorEstado(todas, EstadoSolicitud.PENDIENTE));
        System.out.println("En proceso:  " + contarPorEstado(todas, EstadoSolicitud.EN_PROCESO));
        System.out.println("Resueltas:   " + contarPorEstado(todas, EstadoSolicitud.RESUELTA));
        System.out.println("Rechazadas:  " + contarPorEstado(todas, EstadoSolicitud.RECHAZADA));
        System.out.println("TOTAL:       " + todas.size());

        imprimirDemoraPromedio(todas);
        imprimirVencidas(todas);
    }

    private long contarPorEstado(List<Solicitud> todas, EstadoSolicitud estado) {
        return todas.stream()
                .filter(s -> estado.codigo().equals(s.estado))
                .count();
    }

    private void imprimirDemoraPromedio(List<Solicitud> todas) {
        List<Solicitud> cerradas = todas.stream()
                .filter(s -> s.fechaCierre != null)
                .toList();
        if (cerradas.isEmpty()) {
            return;
        }
        long totalDias = cerradas.stream()
                .mapToLong(s -> ChronoUnit.DAYS.between(s.fechaAlta, s.fechaCierre))
                .sum();
        System.out.println("Demora promedio: " + (totalDias / cerradas.size()) + " dias");
    }

    private void imprimirVencidas(List<Solicitud> todas) {
        System.out.println("--- VENCIDAS (mas de "
                + DIAS_PARA_CONSIDERAR_VENCIDA + " dias) ---");
        todas.stream()
                .filter(this::estaVencida)
                .forEach(s -> System.out.println("  #" + s.id + " (" + s.tipo
                        + ") - prioridad " + s.prioridad));
    }

    private boolean estaVencida(Solicitud solicitud) {
        return solicitud.fechaCierre == null
                && ChronoUnit.DAYS.between(solicitud.fechaAlta, LocalDate.now())
                > DIAS_PARA_CONSIDERAR_VENCIDA;
    }
}
