package mx.velian.angur.auth;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public final class AuthService {
    private static final String DUMMY_HASH =
            "pbkdf2_sha256$210000$AAECAwQFBgcICQoLDA0ODw$14yMQsmlVRLaCMCxJCHUOaZP0RWWeAFuX4_LpZBHo0U";
    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public LoginResult login(String identifier, String password) throws SQLException {
        if (identifier == null || identifier.isBlank() || password == null || password.isEmpty()) {
            return LoginResult.of(LoginResult.Status.REQUIRED_FIELDS);
        }
        String normalized = identifier.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 254 || password.length() > 1024) {
            return LoginResult.of(LoginResult.Status.INVALID_INPUT);
        }
        Optional<UserAccount> account = users.findByIdentifier(normalized);
        char[] candidate = password.toCharArray();
        try {
            boolean verified = PasswordHasher.verify(
                    candidate,
                    account.map(UserAccount::passwordHash).orElse(DUMMY_HASH)
            );
            if (account.isEmpty() || !account.get().active() || !verified) {
                return LoginResult.of(LoginResult.Status.INVALID_CREDENTIALS);
            }
            return new LoginResult(LoginResult.Status.SUCCESS, account.get());
        } finally {
            Arrays.fill(candidate, '\0');
        }
    }
}
