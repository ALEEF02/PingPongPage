package ppp.auth;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import ppp.db.UserRepository;
import ppp.db.model.OUser;
import ppp.meta.LoginEnum;

class AuthenticatorCompareAuthCodeTest {

    @Test
    void testCompareAuthCodeSuccess() {
        Authenticator.emailsSent.put("user@example.com", new Integer[]{(int)(System.currentTimeMillis() / 1000), 12345, 0});
        Authenticator authenticator = new Authenticator(null); // No UserRepository needed for this test.

        LoginEnum.Status status = authenticator.compareAuthCode("user@example.com", 12345);
        assertEquals(LoginEnum.Status.SUCCESS, status);
    }

    @Test
    void testCompareAuthCodeEmailInvalid() {
        Authenticator authenticator = new Authenticator(null);

        LoginEnum.Status status = authenticator.compareAuthCode("nonexistent@example.com", 12345);
        assertEquals(LoginEnum.Status.EMAIL_INVALID, status);
    }

    @Test
    void testCompareAuthCodeTooLate() {
        Authenticator.emailsSent.put("user@example.com", new Integer[]{(int)(System.currentTimeMillis() / 1000 - 3600), 12345, 0});
        Authenticator authenticator = new Authenticator(null);

        LoginEnum.Status status = authenticator.compareAuthCode("user@example.com", 12345);
        assertEquals(LoginEnum.Status.AUTH_TOO_LATE, status);
    }

    @Test
    void testCompareAuthCodeTooManyAttempts() {
        Authenticator.emailsSent.put("user@example.com", new Integer[]{(int)(System.currentTimeMillis() / 1000), 12345, 3});
        Authenticator authenticator = new Authenticator(null);

        LoginEnum.Status status = authenticator.compareAuthCode("user@example.com", 12345);
        assertEquals(LoginEnum.Status.TOO_MANY_ATTEMPTS, status);
    }

    @Test
    void testCompareAuthCodeInvalid() {
        Authenticator.emailsSent.put("user@example.com", new Integer[]{(int)(System.currentTimeMillis() / 1000), 12345, 0});
        Authenticator authenticator = new Authenticator(null);

        LoginEnum.Status status = authenticator.compareAuthCode("user@example.com", 54321);
        assertEquals(LoginEnum.Status.AUTH_INVALID, status);
    }
}
