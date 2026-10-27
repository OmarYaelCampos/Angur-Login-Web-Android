package mx.velian.angur.auth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private Database() {}

    public static Connection connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("No se encontró el controlador de MySQL", ex);
        }
        String url = System.getenv().getOrDefault(
                "ANGUR_DB_URL",
                "jdbc:mysql://127.0.0.1:3306/angur_gestion?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC"
        );
        String user = System.getenv().getOrDefault("ANGUR_DB_USER", "angur_app");
        String password = System.getenv("ANGUR_DB_PASSWORD");
        if (password == null) {
            throw new SQLException("Falta la variable de entorno ANGUR_DB_PASSWORD");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
