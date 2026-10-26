package ar.utn.solicitudes.models;
/**
 * Tipos de solicitud validos. Cada uno conoce su prioridad,
 * que antes se calculaba con una cadena de if/else en el gestor.
 */
public enum TipoDeSolicitud {
    REINCORPORACION("reincorporacion", 1),
    EQUIVALENCIA("equivalencia", 2),
    CERTIFICADO("certificado", 3);

    private final String codigo;
    private final int prioridad;

    TipoDeSolicitud(String codigo, int prioridad) {
        this.codigo = codigo;
        this.prioridad = prioridad;
    }

    public String codigo() {
        return codigo;
    }

    public int prioridad() {
        return prioridad;
    }

    /** Devuelve null si el codigo no corresponde a ningun tipo valido. */
    public static TipoDeSolicitud desdeCodigo(String codigo) {
        for (TipoDeSolicitud tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
                return tipo;
            }
        }
        return null;
    }
}
