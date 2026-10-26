package ar.utn.solicitudes;

import ar.utn.solicitudes.models.DatosDeEstudiante;
import ar.utn.solicitudes.models.Responsable;


public class Main {
    public static void main(String[] args) {
        GestorDeSolicitudes gestor = new GestorDeSolicitudes();
        gestor.inicializar();

        // alta de responsables
        gestor.altaResponsable(new Responsable("R01", "Laura Gimenez", "Bedelia"));
        gestor.altaResponsable(new Responsable("R02", "Marcos Duarte", "Secretaria Academica"));


        DatosDeEstudiante ana = new DatosDeEstudiante(
                "45120", "Ana", "Perez", "ana.perez@mail.com",
                "1155550001", "Tecnicatura en Programacion");

        DatosDeEstudiante bruno = new DatosDeEstudiante(
                "45200", "Bruno", "Lopez", "bruno.lopez@mail.com",
                "1155550002", "Tecnicatura en Programacion");

        System.out.println("--- ALTA DE SOLICITUDES ---");
        int id1 = gestor.registrarSolicitud(ana, "certificado",
                "Certificado de alumno regular");

        int id2 = gestor.registrarSolicitud(ana, "reincorporacion",
                "Reincorporacion por inasistencias");

        int id3 = gestor.registrarSolicitud(bruno, "equivalencia",
                "Equivalencia de Analisis Matematico");

        System.out.println();
        System.out.println("--- CASOS DE ERROR ---");
        gestor.registrarSolicitud(
                new DatosDeEstudiante("", "Sin", "Legajo", "x@mail.com",
                        "1155550003", "Tecnicatura"),
                "certificado", "");

        gestor.registrarSolicitud(
                new DatosDeEstudiante("45300", "Carla", "Diaz", "carla-sin-arroba",
                        "1155550004", "Tecnicatura"),
                "certificado", "");

        gestor.registrarSolicitud(
                new DatosDeEstudiante("45300", "Carla", "Diaz", "carla.diaz@mail.com",
                        "1155550004", "Tecnicatura"),
                "inventado", "");



        System.out.println();
        System.out.println("--- ASIGNACION Y CAMBIO DE ESTADO ---");
        gestor.cambiarEstado(id1, "en_proceso");          // falla: sin responsable
        gestor.asignarResponsable(id1, "R01");
        gestor.cambiarEstado(id1, "en_proceso");          // ahora si
        gestor.cambiarEstado(id1, "resuelta");
        gestor.cambiarEstado(id1, "pendiente");           // falla: ya esta cerrada

        gestor.asignarResponsable(id2, "R02");
        gestor.cambiarEstado(id2, "en_proceso");

        gestor.asignarResponsable(id3, "R99");            // falla: no existe

        System.out.println();
        System.out.println("--- BUSQUEDAS ---");
        System.out.println("Pendientes: " + gestor.buscarPorEstado("pendiente").size());
        System.out.println("En proceso: " + gestor.buscarPorEstado("en_proceso").size());
        System.out.println("Certificados: " + gestor.buscarPorTipo("certificado").size());
        System.out.println("De R01: " + gestor.buscarPorResponsable("R01").size());

        System.out.println();
        gestor.generarReporte();

    }
}