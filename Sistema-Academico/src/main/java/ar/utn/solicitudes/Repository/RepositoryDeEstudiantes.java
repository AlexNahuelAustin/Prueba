package ar.utn.solicitudes.Repository;

import ar.utn.solicitudes.models.DatosDeEstudiante;

public interface RepositoryDeEstudiantes {
    boolean existe(String legajo);

    void guardar(DatosDeEstudiante datos);
}
