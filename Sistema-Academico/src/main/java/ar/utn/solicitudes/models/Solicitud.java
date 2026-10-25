package ar.utn.solicitudes.models;

import java.time.LocalDate;

public class Solicitud {
    public int id;
    public String legajoEstudiante;
    public String tipo;          // "certificado", "equivalencia", "reincorporacion"
    public String estado;        // "pendiente", "en_proceso", "resuelta", "rechazada"
    public int prioridad;        // 1 = alta, 2 = media, 3 = baja
    public String idResponsable;
    public LocalDate fechaAlta;
    public LocalDate fechaCierre;
    public String observaciones;

    public Solicitud(int id, String legajoEstudiante, String tipo) {
        this.id = id;
        this.legajoEstudiante = legajoEstudiante;
        this.tipo = tipo;
        this.estado = "pendiente";
        this.prioridad = 3;
        this.fechaAlta = LocalDate.now();
    }

    public Solicitud() {
    }
}
