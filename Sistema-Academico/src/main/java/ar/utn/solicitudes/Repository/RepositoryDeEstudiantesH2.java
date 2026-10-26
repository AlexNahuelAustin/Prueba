package ar.utn.solicitudes.repository;

import ar.utn.solicitudes.Repository.ConexionH2;
import ar.utn.solicitudes.Repository.ErrorDePersistencia;
import ar.utn.solicitudes.Repository.RepositoryDeEstudiantes;
import ar.utn.solicitudes.models.DatosDeEstudiante;

import java.sql.*;

public class RepositoryDeEstudiantesH2 implements RepositoryDeEstudiantes {

    private final ConexionH2 conexion;

    public RepositoryDeEstudiantesH2(ConexionH2 conexion) {
        this.conexion = conexion;
    }

    @Override
    public boolean existe(String legajo) {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT legajo FROM estudiantes WHERE legajo = ?")) {
            ps.setString(1, legajo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo buscar el estudiante " + legajo, e);
        }
    }

    @Override
    public void guardar(DatosDeEstudiante datos) {
        String sql = "MERGE INTO estudiantes VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, datos.legajo());
            ps.setString(2, datos.nombre());
            ps.setString(3, datos.apellido());
            ps.setString(4, datos.email());
            ps.setString(5, datos.telefono());
            ps.setString(6, datos.carrera());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo guardar el estudiante", e);
        }
    }
}