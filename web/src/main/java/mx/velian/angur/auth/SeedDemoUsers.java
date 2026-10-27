package mx.velian.angur.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;

/** Crea cuentas de práctica; no se invoca desde la aplicación web. */
public final class SeedDemoUsers {
    private static final String INSERT = """
            INSERT IGNORE INTO usuarios (usuario, correo, nombre, clave_hash, rol, activo)
            VALUES (?, ?, ?, ?, ?, TRUE)
            """;

    private SeedDemoUsers() {}

    public static void main(String[] args) throws Exception {
        String seedPassword = System.getenv("ANGUR_SEED_PASSWORD");
        if (seedPassword == null || seedPassword.length() < 12) {
            throw new IllegalArgumentException("ANGUR_SEED_PASSWORD debe tener al menos 12 caracteres");
        }
        char[] password = seedPassword.toCharArray();
        String[][] users = {
                {"administrador", "administrador@angur.local", "Administrador de prueba", "ADMINISTRADOR"},
                {"vendedor", "vendedor@angur.local", "Vendedor de prueba", "VENDEDOR"},
                {"cliente", "cliente@angur.local", "Cliente de prueba", "CLIENTE"},
                {"supervisor", "supervisor@angur.local", "Supervisor de prueba", "SUPERVISOR_VENTAS"},
                {"almacen", "almacen@angur.local", "Almacén de prueba", "ENCARGADO_ALMACEN"}
        };
        try (Connection connection = Database.connect();
             PreparedStatement insert = connection.prepareStatement(INSERT)) {
            for (String[] user : users) {
                insert.setString(1, user[0]);
                insert.setString(2, user[1]);
                insert.setString(3, user[2]);
                insert.setString(4, PasswordHasher.hash(password));
                insert.setString(5, user[3]);
                insert.executeUpdate();
            }
        } finally {
            Arrays.fill(password, '\0');
        }
        System.out.println("Cuentas de prueba preparadas. No se imprimieron contraseñas.");
    }
}
