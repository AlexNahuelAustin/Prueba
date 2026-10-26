package ar.utn.solicitudes;

import ar.utn.solicitudes.Repository.ConexionH2;
import ar.utn.solicitudes.models.DatosDeEstudiante;
import ar.utn.solicitudes.models.Responsable;
import ar.utn.solicitudes.services.GeneradorDeReportes;
import ar.utn.solicitudes.services.ServicioDeSolicitudes;


public class Main {
    public static void main(String[] args) {
        // --- ensamblado de dependencias ---
        ConexionH2 conexion = new ConexionH2();
        conexion.crearTablasSiNoExisten();

        ServicioDeSolicitudes servicio = new ServicioDeSolicitudes(
                new ar.utn.solicitudes.Repository.RepositoryDeSolicitudesH2(conexion),
                new ar.utn.solicitudes.Repository.RepositoryDeEstudiantesH2(conexion),
                new ar.utn.solicitudes.Repository.RepositoryDeResponsablesH2(conexion));

        GeneradorDeReportes reportes = new GeneradorDeReportes(servicio);

        // --- alta de responsables ---
        servicio.altaResponsable(new Responsable("R01", "Laura Gimenez", "Bedelia"));
        servicio.altaResponsable(new Responsable("R02", "Marcos Duarte", "Secretaria Academica"));

        DatosDeEstudiante ana = new DatosDeEstudiante(
                "45120", "Ana", "Perez", "ana.perez@mail.com",
                "1155550001", "Tecnicatura en Programacion");

        DatosDeEstudiante bruno = new DatosDeEstudiante(
                "45200", "Bruno", "Lopez", "bruno.lopez@mail.com",
                "1155550002", "Tecnicatura en Programacion");

        System.out.println("--- ALTA DE SOLICITUDES ---");
        int id1 = servicio.registrarSolicitud(ana, "certificado",
                "Certificado de alumno regular");

        int id2 = servicio.registrarSolicitud(ana, "reincorporacion",
                "Reincorporacion por inasistencias");

        int id3 = servicio.registrarSolicitud(bruno, "equivalencia",
                "Equivalencia de Analisis Matematico");

        System.out.println();
        System.out.println("--- CASOS DE ERROR ---");
        servicio.registrarSolicitud(
                new DatosDeEstudiante("", "Sin", "Legajo", "x@mail.com",
                        "1155550003", "Tecnicatura"),
                "certificado", "");

        servicio.registrarSolicitud(
                new DatosDeEstudiante("45300", "Carla", "Diaz", "carla-sin-arroba",
                        "1155550004", "Tecnicatura"),
                "certificado", "");

        servicio.registrarSolicitud(
                new DatosDeEstudiante("45300", "Carla", "Diaz", "carla.diaz@mail.com",
                        "1155550004", "Tecnicatura"),
                "inventado", "");

        System.out.println();
        System.out.println("--- ASIGNACION Y CAMBIO DE ESTADO ---");
        servicio.cambiarEstado(id1, "en_proceso");          // falla: sin responsable
        servicio.asignarResponsable(id1, "R01");
        servicio.cambiarEstado(id1, "en_proceso");          // ahora si
        servicio.cambiarEstado(id1, "resuelta");
        servicio.cambiarEstado(id1, "pendiente");           // falla: ya esta cerrada

        servicio.asignarResponsable(id2, "R02");
        servicio.cambiarEstado(id2, "en_proceso");

        servicio.asignarResponsable(id3, "R99");            // falla: no existe

        System.out.println();
        System.out.println("--- BUSQUEDAS ---");
        System.out.println("Pendientes: " + servicio.buscarPorEstado("pendiente").size());
        System.out.println("En proceso: " + servicio.buscarPorEstado("en_proceso").size());
        System.out.println("Certificados: " + servicio.buscarPorTipo("certificado").size());
        System.out.println("De R01: " + servicio.buscarPorResponsable("R01").size());

        System.out.println();
        reportes.imprimir();
    }
}