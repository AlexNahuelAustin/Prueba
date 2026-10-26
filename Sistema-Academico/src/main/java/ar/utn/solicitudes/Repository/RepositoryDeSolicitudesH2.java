package ar.utn.solicitudes.Repository;

import ar.utn.solicitudes.Repository.ConexionH2;
import ar.utn.solicitudes.Repository.ErrorDePersistencia;
import ar.utn.solicitudes.Repository.RepositoryDeSolicitudes;
import ar.utn.solicitudes.models.Solicitud;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositoryDeSolicitudesH2 implements RepositoryDeSolicitudes {
    private final ConexionH2 conexion;

    public RepositoryDeSolicitudesH2(ConexionH2 conexion) {
        this.conexion = conexion;
    }

    @Override
    public int siguienteId() {
        try (Connection con = conexion.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(id) FROM solicitudes")) {
            rs.next();
            return rs.getInt(1) + 1;
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo generar el id", e);
        }
    }

    @Override
    public void guardar(Solicitud s) {
        String sql = """
            MERGE INTO solicitudes
            (id, legajo_estudiante, tipo, estado, prioridad,
             id_responsable, fecha_alta, fecha_cierre, observaciones)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, s.id);
            ps.setString(2, s.legajoEstudiante);
            ps.setString(3, s.tipo);
            ps.setString(4, s.estado);
            ps.setInt(5, s.prioridad);
            ps.setString(6, s.idResponsable);
            ps.setDate(7, Date.valueOf(s.fechaAlta));
            ps.setDate(8, s.fechaCierre == null ? null : Date.valueOf(s.fechaCierre));
            ps.setString(9, s.observaciones);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo guardar la solicitud " + s.id, e);
        }
    }

    @Override
    public Optional<Solicitud> buscarPorId(int id) {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM solicitudes WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo buscar la solicitud " + id, e);
        }
    }

    @Override
    public List<Solicitud> buscarPorEstado(String estado) {
        return buscarPor("estado", estado);
    }

    @Override
    public List<Solicitud> buscarPorTipo(String tipo) {
        return buscarPor("tipo", tipo);
    }

    @Override
    public List<Solicitud> buscarPorResponsable(String idResponsable) {
        return buscarPor("id_responsable", idResponsable);
    }

    @Override
    public List<Solicitud> buscarTodas() {
        try (Connection con = conexion.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM solicitudes")) {
            return mapearTodas(rs);
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudieron listar las solicitudes", e);
        }
    }

    @Override
    public int contarAbiertasDe(String legajo) {
        String sql = """
            SELECT COUNT(*) FROM solicitudes
            WHERE legajo_estudiante = ? AND fecha_cierre IS NULL
            """;
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, legajo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudieron contar las abiertas", e);
        }
    }

    @Override
    public void actualizarEstado(int id, String nuevoEstado, LocalDate fechaCierre) {
        String sql = "UPDATE solicitudes SET estado = ?, fecha_cierre = ? WHERE id = ?";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setDate(2, fechaCierre == null ? null : Date.valueOf(fechaCierre));
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo actualizar el estado de " + id, e);
        }
    }

    @Override
    public void actualizarResponsable(int id, String idResponsable) {
        String sql = "UPDATE solicitudes SET id_responsable = ? WHERE id = ?";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idResponsable);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo asignar el responsable a " + id, e);
        }
    }

    // ---------- privados ----------

    private List<Solicitud> buscarPor(String columna, String valor) {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM solicitudes WHERE " + columna + " = ?")) {
            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return mapearTodas(rs);
            }
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo buscar por " + columna, e);
        }
    }

    private List<Solicitud> mapearTodas(ResultSet rs) throws SQLException {
        List<Solicitud> solicitudes = new ArrayList<>();
        while (rs.next()) {
            solicitudes.add(mapear(rs));
        }
        return solicitudes;
    }

    /** Unico lugar donde se traduce una fila a un objeto de dominio. */
    private Solicitud mapear(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.id = rs.getInt("id");
        s.legajoEstudiante = rs.getString("legajo_estudiante");
        s.tipo = rs.getString("tipo");
        s.estado = rs.getString("estado");
        s.prioridad = rs.getInt("prioridad");
        s.idResponsable = rs.getString("id_responsable");
        s.fechaAlta = rs.getDate("fecha_alta").toLocalDate();
        Date cierre = rs.getDate("fecha_cierre");
        s.fechaCierre = (cierre == null) ? null : cierre.toLocalDate();
        s.observaciones = rs.getString("observaciones");
        return s;
    }
}
