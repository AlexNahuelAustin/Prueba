package ar.utn.solicitudes.dobles;

import ar.utn.solicitudes.Repository.RepositoryDeEstudiantes;
import ar.utn.solicitudes.models.DatosDeEstudiante;

import java.util.ArrayList;
import java.util.List;

public class RepositoryDeEstudiantesEnMemoria implements RepositoryDeEstudiantes {
    private final List<DatosDeEstudiante> estudiantes = new ArrayList<>();

    @Override
    public boolean existe(String legajo) {
        return estudiantes.stream().anyMatch(e -> e.legajo().equals(legajo));
    }

    @Override
    public void guardar(DatosDeEstudiante datos) {
        estudiantes.removeIf(e -> e.legajo().equals(datos.legajo()));
        estudiantes.add(datos);
    }
}
