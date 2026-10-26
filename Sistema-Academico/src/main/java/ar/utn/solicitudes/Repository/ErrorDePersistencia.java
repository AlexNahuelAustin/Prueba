package ar.utn.solicitudes.Repository;
/**
 * Envuelve los errores de infraestructura para que el dominio
 * no tenga que conocer SQLException.
 */
public class ErrorDePersistencia extends RuntimeException {
    public ErrorDePersistencia(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
