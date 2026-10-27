package mx.velian.angur.auth;

public record UserAccount(
        long id,
        String username,
        String email,
        String displayName,
        String passwordHash,
        String role,
        boolean active
) {}
