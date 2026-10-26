package ar.utn.solicitudes.models;
/**
 * Agrupa los datos de identificacion y contacto del estudiante,
 * que antes viajaban como seis parametros sueltos.
 */
public record DatosDeEstudiante (
        String legajo,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String carrera)
{

}

