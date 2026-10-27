package mx.velian.angur.auth;

public record LoginResult(Status status, UserAccount user) {
    public enum Status { SUCCESS, REQUIRED_FIELDS, INVALID_INPUT, INVALID_CREDENTIALS }

    public static LoginResult of(Status status) {
        return new LoginResult(status, null);
    }
}
