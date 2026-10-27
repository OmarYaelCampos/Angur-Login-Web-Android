package mx.velian.angur.auth;

import java.sql.SQLException;
import java.util.Optional;

public interface UserRepository {
    Optional<UserAccount> findByIdentifier(String identifier) throws SQLException;
}
