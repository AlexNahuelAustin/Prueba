package ar.utn.solicitudes.models;

import static java.time.chrono.JapaneseEra.values;

public enum EstadoSolicitud {
    PENDIENTE("pendiente"),
    EN_PROCESO("en_proceso"),
    RESUELTA("resuelta"),
    RECHAZADA("rechazada");

    private final String codigo;

    EstadoSolicitud(String codigo) {
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }

    public boolean esFinal() {
        return this == RESUELTA || this == RECHAZADA;
    }

    public static EstadoSolicitud desdeCodigo(String codigo) {
        for (EstadoSolicitud estado : values()) {
            if (estado.codigo.equals(codigo)) {
                return estado;
            }
        }
        return null;
    }
}
