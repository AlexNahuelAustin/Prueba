package ar.utn.solicitudes.dobles.ar.utn.solicitudes;

import ar.utn.solicitudes.dobles.RepositoryDeEstudiantesEnMemoria;
import ar.utn.solicitudes.dobles.RepositoryDeResponsablesEnMemoria;
import ar.utn.solicitudes.dobles.RepositoryDeSolicitudesEnMemoria;
import ar.utn.solicitudes.models.DatosDeEstudiante;
import ar.utn.solicitudes.models.Responsable;
import ar.utn.solicitudes.services.ServicioDeSolicitudes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica que el comportamiento externo del modulo se mantiene
 * despues del refactoring. Usa repositorios en memoria: no necesita base de datos.
 */
public class services {
    private ServicioDeSolicitudes servicio;
    private RepositoryDeSolicitudesEnMemoria solicitudes;
    private DatosDeEstudiante ana;

    @BeforeEach
    void prepararEscenario() {
        solicitudes = new RepositoryDeSolicitudesEnMemoria();
        servicio = new ServicioDeSolicitudes(
                solicitudes,
                new RepositoryDeEstudiantesEnMemoria(),
                new RepositoryDeResponsablesEnMemoria());

        servicio.altaResponsable(new Responsable("R01", "Laura Gimenez", "Bedelia"));

        ana = new DatosDeEstudiante("45120", "Ana", "Perez",
                "ana.perez@mail.com", "1155550001", "Tecnicatura en Programacion");
    }

    // ---------- ALTA ----------

    @Test
    @DisplayName("Una solicitud valida se registra y devuelve un id positivo")
    void registrarSolicitudValida_devuelveId() {
        // Act
        int id = servicio.registrarSolicitud(ana, "certificado", "Alumno regular");

        // Assert
        assertTrue(id > 0);
        assertTrue(servicio.buscarPorId(id).isPresent());
    }

    @Test
    @DisplayName("Sin legajo la solicitud se rechaza")
    void registrarSinLegajo_devuelveMenosUno() {
        // Arrange
        DatosDeEstudiante sinLegajo = new DatosDeEstudiante("", "Sin", "Legajo",
                "x@mail.com", "1155550003", "Tecnicatura");

        // Act
        int id = servicio.registrarSolicitud(sinLegajo, "certificado", "");

        // Assert
        assertEquals(-1, id);
    }

    @Test
    @DisplayName("Con email sin arroba la solicitud se rechaza")
    void registrarConEmailInvalido_devuelveMenosUno() {
        // Arrange
        DatosDeEstudiante emailMalo = new DatosDeEstudiante("45300", "Carla", "Diaz",
                "carla-sin-arroba", "1155550004", "Tecnicatura");

        // Act
        int id = servicio.registrarSolicitud(emailMalo, "certificado", "");

        // Assert
        assertEquals(-1, id);
    }

    @Test
    @DisplayName("Con un tipo inexistente la solicitud se rechaza")
    void registrarConTipoInvalido_devuelveMenosUno() {
        // Act
        int id = servicio.registrarSolicitud(ana, "inventado", "");

        // Assert
        assertEquals(-1, id);
    }

    @Test
    @DisplayName("La prioridad se asigna segun el tipo de solicitud")
    void registrarSolicitud_asignaPrioridadSegunTipo() {
        // Act
        int reincorporacion = servicio.registrarSolicitud(ana, "reincorporacion", "");
        int equivalencia = servicio.registrarSolicitud(ana, "equivalencia", "");
        int certificado = servicio.registrarSolicitud(ana, "certificado", "");

        // Assert
        assertEquals(1, servicio.buscarPorId(reincorporacion).get().prioridad);
        assertEquals(2, servicio.buscarPorId(equivalencia).get().prioridad);
        assertEquals(3, servicio.buscarPorId(certificado).get().prioridad);
    }

    @Test
    @DisplayName("Caso de borde: no se permiten mas de 5 solicitudes abiertas")
    void registrarSextaSolicitudAbierta_devuelveMenosUno() {
        // Arrange
        for (int i = 0; i < 5; i++) {
            servicio.registrarSolicitud(ana, "certificado", "");
        }

        // Act
        int sexta = servicio.registrarSolicitud(ana, "certificado", "");

        // Assert
        assertEquals(-1, sexta);
    }

    // ---------- CAMBIO DE ESTADO ----------

    @Test
    @DisplayName("No se puede poner en proceso una solicitud sin responsable")
    void cambiarAEnProcesoSinResponsable_falla() {
        // Arrange
        int id = servicio.registrarSolicitud(ana, "certificado", "");

        // Act
        boolean resultado = servicio.cambiarEstado(id, "en_proceso");

        // Assert
        assertFalse(resultado);
        assertEquals("pendiente", servicio.buscarPorId(id).get().estado);
    }

    @Test
    @DisplayName("Con responsable asignado, pendiente pasa a en proceso")
    void cambiarAEnProcesoConResponsable_funciona() {
        // Arrange
        int id = servicio.registrarSolicitud(ana, "certificado", "");
        servicio.asignarResponsable(id, "R01");

        // Act
        boolean resultado = servicio.cambiarEstado(id, "en_proceso");

        // Assert
        assertTrue(resultado);
        assertEquals("en_proceso", servicio.buscarPorId(id).get().estado);
    }

    @Test
    @DisplayName("Una transicion invalida se rechaza")
    void transicionInvalida_falla() {
        // Arrange
        int id = servicio.registrarSolicitud(ana, "certificado", "");

        // Act
        boolean resultado = servicio.cambiarEstado(id, "resuelta");

        // Assert
        assertFalse(resultado);
    }

    @Test
    @DisplayName("Una solicitud cerrada no se puede reabrir")
    void reabrirSolicitudCerrada_falla() {
        // Arrange
        int id = servicio.registrarSolicitud(ana, "certificado", "");
        servicio.asignarResponsable(id, "R01");
        servicio.cambiarEstado(id, "en_proceso");
        servicio.cambiarEstado(id, "resuelta");

        // Act
        boolean resultado = servicio.cambiarEstado(id, "pendiente");

        // Assert
        assertFalse(resultado);
        assertEquals("resuelta", servicio.buscarPorId(id).get().estado);
    }

    @Test
    @DisplayName("Al resolverse se registra la fecha de cierre")
    void resolverSolicitud_registraFechaDeCierre() {
        // Arrange
        int id = servicio.registrarSolicitud(ana, "certificado", "");
        servicio.asignarResponsable(id, "R01");
        servicio.cambiarEstado(id, "en_proceso");

        // Act
        servicio.cambiarEstado(id, "resuelta");

        // Assert
        assertNotNull(servicio.buscarPorId(id).get().fechaCierre);
    }

    // ---------- ASIGNACION ----------

    @Test
    @DisplayName("Asignar un responsable inexistente falla")
    void asignarResponsableInexistente_falla() {
        // Arrange
        int id = servicio.registrarSolicitud(ana, "certificado", "");

        // Act
        boolean resultado = servicio.asignarResponsable(id, "R99");

        // Assert
        assertFalse(resultado);
    }

    @Test
    @DisplayName("Asignar sobre una solicitud inexistente falla")
    void asignarASolicitudInexistente_falla() {
        // Act
        boolean resultado = servicio.asignarResponsable(999, "R01");

        // Assert
        assertFalse(resultado);
    }

    // ---------- BUSQUEDAS ----------

    @Test
    @DisplayName("La busqueda por estado devuelve solo las que coinciden")
    void buscarPorEstado_filtraCorrectamente() {
        // Arrange
        int id1 = servicio.registrarSolicitud(ana, "certificado", "");
        servicio.registrarSolicitud(ana, "equivalencia", "");
        servicio.asignarResponsable(id1, "R01");
        servicio.cambiarEstado(id1, "en_proceso");

        // Act
        var pendientes = servicio.buscarPorEstado("pendiente");
        var enProceso = servicio.buscarPorEstado("en_proceso");

        // Assert
        assertEquals(1, pendientes.size());
        assertEquals(1, enProceso.size());
    }
}
