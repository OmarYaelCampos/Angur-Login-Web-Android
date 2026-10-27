package mx.velian.angurgestion;

public final class LoginValidator {
    public enum Result { VALID, MISSING_IDENTIFIER, MISSING_PASSWORD, MISSING_BOTH }

    private LoginValidator() {}

    public static Result validate(String identifier, String password) {
        boolean noIdentifier = identifier == null || identifier.trim().isEmpty();
        boolean noPassword = password == null || password.isEmpty();
        if (noIdentifier && noPassword) return Result.MISSING_BOTH;
        if (noIdentifier) return Result.MISSING_IDENTIFIER;
        if (noPassword) return Result.MISSING_PASSWORD;
        return Result.VALID;
    }
}
