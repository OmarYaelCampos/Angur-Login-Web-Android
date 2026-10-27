package mx.velian.angurgestion;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LoginValidatorTest {
    @Test public void emptyFieldsAreRejected() {
        assertEquals(LoginValidator.Result.MISSING_BOTH, LoginValidator.validate("", ""));
    }

    @Test public void whitespaceIdentifierIsRejected() {
        assertEquals(LoginValidator.Result.MISSING_IDENTIFIER, LoginValidator.validate("  ", "clave"));
    }

    @Test public void completeFieldsAreAcceptedForServerValidation() {
        assertEquals(LoginValidator.Result.VALID, LoginValidator.validate("vendedor", "clave"));
    }
}
