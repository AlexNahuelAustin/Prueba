package ar.utn.solicitudes.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Centraliza los datos de conexion y la creacion de las tablas.
 * Antes estaban repetidos en cada metodo del gestor.
 */
public class ConexionH2 {
    private final String url;
    private final String usuario;
    private final String clave;

    public ConexionH2(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    public ConexionH2() {
        this("jdbc:h2:./datos/academico", "sa", "");
    }

    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    public void crearTablasSiNoExisten() {
        try (Connection con = abrir(); Statement st = con.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS estudiantes (
                    legajo VARCHAR(20) PRIMARY KEY, nombre VARCHAR(50),
                    apellido VARCHAR(50), email VARCHAR(100),
                    telefono VARCHAR(30), carrera VARCHAR(100))
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS responsables (
                    id VARCHAR(10) PRIMARY KEY, nombre VARCHAR(50),
                    area VARCHAR(50), cantidad_asignada INT)
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS solicitudes (
                    id INT PRIMARY KEY, legajo_estudiante VARCHAR(20),
                    tipo VARCHAR(30), estado VARCHAR(30), prioridad INT,
                    id_responsable VARCHAR(10), fecha_alta DATE,
                    fecha_cierre DATE, observaciones VARCHAR(500))
                """);
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudieron crear las tablas", e);
        }
    }
}
