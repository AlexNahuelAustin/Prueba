package ar.utn.solicitudes.repository;

import ar.utn.solicitudes.Repository.ConexionH2;
import ar.utn.solicitudes.Repository.ErrorDePersistencia;
import ar.utn.solicitudes.Repository.RepositoryDeResponsables;
import ar.utn.solicitudes.models.Responsable;

import java.sql.*;
import java.util.Optional;

public class RepositoryDeResponsablesH2 implements RepositoryDeResponsables {

    private final ConexionH2 conexion;

    public RepositoryDeResponsablesH2(ConexionH2 conexion) {
        this.conexion = conexion;
    }

    @Override
    public Optional<Responsable> buscarPorId(String id) {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM responsables WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Responsable r = new Responsable(rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("area"));
                r.cantidadAsignada = rs.getInt("cantidad_asignada");
                return Optional.of(r);
            }
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo buscar el responsable " + id, e);
        }
    }

    @Override
    public void guardar(Responsable r) {
        String sql = "MERGE INTO responsables VALUES (?, ?, ?, ?)";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.id);
            ps.setString(2, r.nombre);
            ps.setString(3, r.area);
            ps.setInt(4, r.cantidadAsignada);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo guardar el responsable", e);
        }
    }

    @Override
    public void incrementarAsignadas(String id) {
        String sql = "UPDATE responsables SET cantidad_asignada = cantidad_asignada + 1 WHERE id = ?";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo incrementar la carga de " + id, e);
        }
    }
}