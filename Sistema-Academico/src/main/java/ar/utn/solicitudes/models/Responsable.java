package ar.utn.solicitudes.models;

public class Responsable {
    public String id;
    public String nombre;
    public String area;
    public int cantidadAsignada;

    public Responsable(String id, String nombre, String area) {
        this.id = id;
        this.nombre = nombre;
        this.area = area;
        this.cantidadAsignada = 0;
    }
}
