package ar.utn.solicitudes;
import ar.utn.solicitudes.models.DatosDeEstudiante;
import ar.utn.solicitudes.models.Responsable;
import ar.utn.solicitudes.models.Solicitud;
import ar.utn.solicitudes.models.EstadoSolicitud;
import ar.utn.solicitudes.models.TipoDeSolicitud;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


/**
 * Gestor principal del sistema de solicitudes academicas.
 * Maneja el alta, el cambio de estado, la asignacion de responsables,
 * las busquedas, el reporte y el acceso a la base de datos.
 */
public class GestorDeSolicitudes {
    private static final int MAXIMO_SOLICITUDES_ABIERTAS_POR_ESTUDIANTE = 5;
    private static final int MAXIMO_ASIGNACIONES_POR_RESPONSABLE = 10;
    private static final int DIAS_PARA_CONSIDERAR_VENCIDA = 30;

    public String url = "jdbc:h2:./datos/academico";
    public String usuario = "sa";
    public String clave = "";

    public void inicializar() {
        try {
            Connection con = DriverManager.getConnection(url, usuario, clave);
            Statement st = con.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS estudiantes ("
                    + "legajo VARCHAR(20) PRIMARY KEY, nombre VARCHAR(50), "
                    + "apellido VARCHAR(50), email VARCHAR(100), "
                    + "telefono VARCHAR(30), carrera VARCHAR(100))");
            st.execute("CREATE TABLE IF NOT EXISTS responsables ("
                    + "id VARCHAR(10) PRIMARY KEY, nombre VARCHAR(50), "
                    + "area VARCHAR(50), cantidad_asignada INT)");
            st.execute("CREATE TABLE IF NOT EXISTS solicitudes ("
                    + "id INT PRIMARY KEY, legajo_estudiante VARCHAR(20), "
                    + "tipo VARCHAR(30), estado VARCHAR(30), prioridad INT, "
                    + "id_responsable VARCHAR(10), fecha_alta DATE, "
                    + "fecha_cierre DATE, observaciones VARCHAR(500))");
            st.close();
            con.close();
        } catch (Exception ex) {
            System.out.println("ERROR al inicializar: " + ex.getMessage());
        }
    }

    /**
     * Da de alta una solicitud. Si el estudiante no existe lo crea.
     * Devuelve el id generado, o -1 si hubo algun error.
     */
    public int registrarSolicitud(DatosDeEstudiante datos, String tipo, String obs) {

        // validaciones
        if (datos.legajo() == null || datos.legajo().equals("")) {
            System.out.println("ERROR: falta el legajo");
            return -1;
        }
        if (datos.email() == null || !datos.email().contains("@")) {
            System.out.println("ERROR: email invalido");
            return -1;
        }
        TipoDeSolicitud tipoDeSolicitud = TipoDeSolicitud.desdeCodigo(tipo);
        if (tipoDeSolicitud == null) {
            System.out.println("ERROR: tipo invalido");
            return -1;
        }

        try {
            Connection con = DriverManager.getConnection(url, usuario, clave);
            Statement st = con.createStatement();

            // busco el estudiante, si no esta lo doy de alta
            ResultSet rs = st.executeQuery(
                    "SELECT * FROM estudiantes WHERE legajo = '" + datos.legajo() + "'");
            boolean existe = rs.next();
            rs.close();
            if (!existe) {
                st.executeUpdate("INSERT INTO estudiantes VALUES ('"
                        + datos.legajo() + "','" + datos.nombre() + "','"
                        + datos.apellido() + "','" + datos.email() + "','"
                        + datos.telefono() + "','" + datos.carrera() + "')");
            }

            // cuento cuantas tiene abiertas (no resueltas ni rechazadas)
            ResultSet rs2 = st.executeQuery(
                    "SELECT COUNT(*) FROM solicitudes WHERE legajo_estudiante = '"
                            + datos.legajo() + "' AND estado <> '" + EstadoSolicitud.RESUELTA.codigo()
                            + "' AND estado <> '" + EstadoSolicitud.RECHAZADA.codigo() + "'");
            rs2.next();
            int cant = rs2.getInt(1);
            rs2.close();

            if (cant >= MAXIMO_SOLICITUDES_ABIERTAS_POR_ESTUDIANTE) {
                System.out.println("ERROR: el estudiante tiene demasiadas solicitudes abiertas");
                st.close();
                con.close();
                return -1;
            }

            ResultSet rs3 = st.executeQuery("SELECT MAX(id) FROM solicitudes");
            rs3.next();
            int nuevoId = rs3.getInt(1) + 1;
            rs3.close();

            st.executeUpdate("INSERT INTO solicitudes VALUES ("
                    + nuevoId + ",'" + datos.legajo() + "','" + tipo + "','"
                    + EstadoSolicitud.PENDIENTE.codigo() + "',"
                    + tipoDeSolicitud.prioridad() + ",NULL,'" + LocalDate.now()
                    + "',NULL,'" + obs + "')");

            st.close();
            con.close();

            System.out.println("Solicitud " + nuevoId + " registrada correctamente");
            return nuevoId;

        } catch (Exception ex) {
            System.out.println("ERROR en la base: " + ex.getMessage());
            return -1;
        }
    }

    /**
     * Cambia el estado de una solicitud validando que la transicion sea valida.
     * pendiente -> en_proceso | rechazada
     * en_proceso -> resuelta | rechazada
     * resuelta y rechazada son estados finales
     */
    public boolean cambiarEstado(int id, String nuevoEstado) {
        try {
            Connection con = DriverManager.getConnection(url, usuario, clave);
            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(
                    "SELECT estado, id_responsable FROM solicitudes WHERE id = " + id);
            if (!rs.next()) {
                System.out.println("ERROR: no existe la solicitud");
                rs.close(); st.close(); con.close();
                return false;
            }
            String actual = rs.getString("estado");
            String resp = rs.getString("id_responsable");
            rs.close();

            String fechaCierre = "NULL";

            if (actual.equals(EstadoSolicitud.PENDIENTE.codigo())) {
                if (nuevoEstado.equals(EstadoSolicitud.EN_PROCESO.codigo())) {
                    // no se puede poner en proceso sin alguien asignado
                    if (resp == null) {
                        System.out.println("ERROR: no se puede procesar sin responsable");
                        st.close(); con.close();
                        return false;
                    }
                } else if (nuevoEstado.equals(EstadoSolicitud.RECHAZADA.codigo())) {
                    fechaCierre = "'" + LocalDate.now() + "'";
                } else {
                    System.out.println("ERROR: transicion invalida");
                    st.close(); con.close();
                    return false;
                }
            } else if (actual.equals(EstadoSolicitud.EN_PROCESO.codigo())) {
                if (nuevoEstado.equals(EstadoSolicitud.RESUELTA.codigo())
                        || nuevoEstado.equals(EstadoSolicitud.RECHAZADA.codigo())) {
                    fechaCierre = "'" + LocalDate.now() + "'";
                } else {
                    System.out.println("ERROR: transicion invalida");
                    st.close(); con.close();
                    return false;
                }
            } else {
                System.out.println("ERROR: la solicitud ya esta cerrada");
                st.close(); con.close();
                return false;
            }

            st.executeUpdate("UPDATE solicitudes SET estado = '" + nuevoEstado
                    + "', fecha_cierre = " + fechaCierre + " WHERE id = " + id);

            st.close();
            con.close();

            System.out.println("Solicitud " + id + " paso a " + nuevoEstado);
            return true;

        } catch (Exception ex) {
            System.out.println("ERROR en la base: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Asigna un responsable a una solicitud si no esta sobrecargado.
     */
    public boolean asignarResponsable(int id, String idResp) {
        try {
            Connection con = DriverManager.getConnection(url, usuario, clave);
            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("SELECT id FROM solicitudes WHERE id = " + id);
            if (!rs.next()) {
                System.out.println("ERROR: no existe la solicitud");
                rs.close(); st.close(); con.close();
                return false;
            }
            rs.close();

            ResultSet rs2 = st.executeQuery(
                    "SELECT nombre, cantidad_asignada FROM responsables WHERE id = '"
                            + idResp + "'");
            if (!rs2.next()) {
                System.out.println("ERROR: no existe el responsable");
                rs2.close(); st.close(); con.close();
                return false;
            }
            String nombreResp = rs2.getString("nombre");
            int asignadas = rs2.getInt("cantidad_asignada");
            rs2.close();

            if (asignadas >= MAXIMO_ASIGNACIONES_POR_RESPONSABLE) {
                System.out.println("ERROR: el responsable esta sobrecargado");
                st.close(); con.close();
                return false;
            }

            st.executeUpdate("UPDATE solicitudes SET id_responsable = '"
                    + idResp + "' WHERE id = " + id);
            st.executeUpdate("UPDATE responsables SET cantidad_asignada = "
                    + (asignadas + 1) + " WHERE id = '" + idResp + "'");

            st.close();
            con.close();

            System.out.println("Solicitud " + id + " asignada a " + nombreResp);
            return true;

        } catch (Exception ex) {
            System.out.println("ERROR en la base: " + ex.getMessage());
            return false;
        }
    }

    public void altaResponsable(Responsable r) {
        try {
            Connection con = DriverManager.getConnection(url, usuario, clave);
            Statement st = con.createStatement();
            st.executeUpdate("MERGE INTO responsables VALUES ('" + r.id + "','"
                    + r.nombre + "','" + r.area + "'," + r.cantidadAsignada + ")");
            st.close();
            con.close();
        } catch (Exception ex) {
            System.out.println("ERROR en la base: " + ex.getMessage());
        }
    }

    // devuelve las solicitudes que estan en el estado indicado
    public List<Solicitud> buscarPorEstado(String estado) {
        List<Solicitud> lst = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(url, usuario, clave);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT * FROM solicitudes WHERE estado = '" + estado + "'");
            while (rs.next()) {
                Solicitud s = new Solicitud();
                s.id = rs.getInt("id");
                s.legajoEstudiante = rs.getString("legajo_estudiante");
                s.tipo = rs.getString("tipo");
                s.estado = rs.getString("estado");
                s.prioridad = rs.getInt("prioridad");
                s.idResponsable = rs.getString("id_responsable");
                s.fechaAlta = rs.getDate("fecha_alta").toLocalDate();
                if (rs.getDate("fecha_cierre") != null) {
                    s.fechaCierre = rs.getDate("fecha_cierre").toLocalDate();
                }
                s.observaciones = rs.getString("observaciones");
                lst.add(s);
            }
            rs.close(); st.close(); con.close();
        } catch (Exception ex) {
            System.out.println("ERROR en la base: " + ex.getMessage());
        }
        return lst;
    }

    // devuelve las solicitudes del tipo indicado
    public List<Solicitud> buscarPorTipo(String tipo) {
        List<Solicitud> lst = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(url, usuario, clave);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT * FROM solicitudes WHERE tipo = '" + tipo + "'");
            while (rs.next()) {
                Solicitud s = new Solicitud();
                s.id = rs.getInt("id");
                s.legajoEstudiante = rs.getString("legajo_estudiante");
                s.tipo = rs.getString("tipo");
                s.estado = rs.getString("estado");
                s.prioridad = rs.getInt("prioridad");
                s.idResponsable = rs.getString("id_responsable");
                s.fechaAlta = rs.getDate("fecha_alta").toLocalDate();
                if (rs.getDate("fecha_cierre") != null) {
                    s.fechaCierre = rs.getDate("fecha_cierre").toLocalDate();
                }
                s.observaciones = rs.getString("observaciones");
                lst.add(s);
            }
            rs.close(); st.close(); con.close();
        } catch (Exception ex) {
            System.out.println("ERROR en la base: " + ex.getMessage());
        }
        return lst;
    }

    // devuelve las solicitudes asignadas a ese responsable
    public List<Solicitud> buscarPorResponsable(String idResp) {
        List<Solicitud> lst = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(url, usuario, clave);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT * FROM solicitudes WHERE id_responsable = '" + idResp + "'");
            while (rs.next()) {
                Solicitud s = new Solicitud();
                s.id = rs.getInt("id");
                s.legajoEstudiante = rs.getString("legajo_estudiante");
                s.tipo = rs.getString("tipo");
                s.estado = rs.getString("estado");
                s.prioridad = rs.getInt("prioridad");
                s.idResponsable = rs.getString("id_responsable");
                s.fechaAlta = rs.getDate("fecha_alta").toLocalDate();
                if (rs.getDate("fecha_cierre") != null) {
                    s.fechaCierre = rs.getDate("fecha_cierre").toLocalDate();
                }
                s.observaciones = rs.getString("observaciones");
                lst.add(s);
            }
            rs.close(); st.close(); con.close();
        } catch (Exception ex) {
            System.out.println("ERROR en la base: " + ex.getMessage());
        }
        return lst;
    }

    /**
     * Imprime por consola el reporte general: totales por estado,
     * demora promedio de cierre y listado de solicitudes vencidas.
     */
    public void generarReporte() {
        System.out.println("========================================");
        System.out.println("   REPORTE DE SOLICITUDES ACADEMICAS");
        System.out.println("========================================");

        try {
            Connection con = DriverManager.getConnection(url, usuario, clave);
            Statement st = con.createStatement();

            // p = pendientes, ep = en proceso, res = resueltas, rech = rechazadas
            int p = 0, ep = 0, res = 0, rech = 0, total = 0;
            ResultSet rs = st.executeQuery("SELECT estado FROM solicitudes");
            while (rs.next()) {
                String e = rs.getString("estado");
                if (e.equals(EstadoSolicitud.PENDIENTE.codigo())) p++;
                else if (e.equals(EstadoSolicitud.EN_PROCESO.codigo())) ep++;
                else if (e.equals(EstadoSolicitud.RESUELTA.codigo())) res++;
                else if (e.equals(EstadoSolicitud.RECHAZADA.codigo())) rech++;
                total++;
            }
            rs.close();

            System.out.println("Pendientes:  " + p);
            System.out.println("En proceso:  " + ep);
            System.out.println("Resueltas:   " + res);
            System.out.println("Rechazadas:  " + rech);
            System.out.println("TOTAL:       " + total);

            // calculo la demora promedio sobre las que ya cerraron
            long sum = 0;
            int c = 0;
            ResultSet rs2 = st.executeQuery(
                    "SELECT fecha_alta, fecha_cierre FROM solicitudes "
                            + "WHERE fecha_cierre IS NOT NULL");
            while (rs2.next()) {
                sum += ChronoUnit.DAYS.between(rs2.getDate("fecha_alta").toLocalDate(),
                        rs2.getDate("fecha_cierre").toLocalDate());
                c++;
            }
            rs2.close();
            if (c > 0) {
                System.out.println("Demora promedio: " + (sum / c) + " dias");
            }

            System.out.println("--- VENCIDAS (mas de "
                    + DIAS_PARA_CONSIDERAR_VENCIDA + " dias) ---");
            ResultSet rs3 = st.executeQuery(
                    "SELECT id, tipo, prioridad, fecha_alta FROM solicitudes "
                            + "WHERE fecha_cierre IS NULL");
            while (rs3.next()) {
                if (ChronoUnit.DAYS.between(rs3.getDate("fecha_alta").toLocalDate(),
                        LocalDate.now()) > DIAS_PARA_CONSIDERAR_VENCIDA) {
                    System.out.println("  #" + rs3.getInt("id") + " ("
                            + rs3.getString("tipo") + ") - prioridad "
                            + rs3.getInt("prioridad"));
                }
            }
            rs3.close();

            st.close();
            con.close();

        } catch (Exception ex) {
            System.out.println("ERROR en la base: " + ex.getMessage());
        }
    }
}
