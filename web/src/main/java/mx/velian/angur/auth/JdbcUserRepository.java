package mx.velian.angur.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class JdbcUserRepository implements UserRepository {
    private static final String FIND = """
            SELECT id, usuario, correo, nombre, clave_hash, rol, activo
            FROM usuarios
            WHERE usuario = ? OR correo = ?
            LIMIT 1
            """;

    @Override
    public Optional<UserAccount> findByIdentifier(String identifier) throws SQLException {
        try (Connection connection = Database.connect();
             PreparedStatement query = connection.prepareStatement(FIND)) {
            query.setString(1, identifier);
            query.setString(2, identifier);
            try (ResultSet rows = query.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                return Optional.of(new UserAccount(
                        rows.getLong("id"),
                        rows.getString("usuario"),
                        rows.getString("correo"),
                        rows.getString("nombre"),
                        rows.getString("clave_hash"),
                        rows.getString("rol"),
                        rows.getBoolean("activo")
                ));
            }
        }
    }
}
