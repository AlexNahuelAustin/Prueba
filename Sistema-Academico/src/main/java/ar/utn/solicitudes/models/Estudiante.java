package ar.utn.solicitudes.models;

public class Estudiante {
    public String legajo;
    public String nombre;
    public String apellido;
    public String email;
    public String telefono;
    public String carrera;

    public Estudiante(String legajo, String nombre, String apellido,
                      String email, String telefono, String carrera) {
        this.legajo = legajo;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.carrera = carrera;
    }
}
