package ppp.auth;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import ppp.meta.LoginEnum;

class AuthenticatorSendAuthEmailTest {

    @Test
    void testSendAuthEmailAlreadySent() {
        Authenticator.emailsSent.put("user@example.com", new Integer[]{(int)(System.currentTimeMillis() / 1000), 12345, 0});
        Authenticator authenticator = new Authenticator(null);

        LoginEnum.Status status = authenticator.sendAuthEmail("user@example.com");
        assertEquals(LoginEnum.Status.AUTH_ALREADY_SENT, status);
    }

    @Test
    void testSendAuthEmailInvalidAddress() {
        Authenticator authenticator = new Authenticator(null);
        LoginEnum.Status status = authenticator.sendAuthEmail("invalid_email");
        assertNotEquals(LoginEnum.Status.EMAIL_SENT, status); // Email sanitization TODO
    }

    @Test
    void testSendAuthEmailRateLimit() {
        Authenticator.emailsSent.put("user@example.com", new Integer[]{(int)(System.currentTimeMillis() / 1000 - 500), 12345, 0});
        Authenticator authenticator = new Authenticator(null);

        LoginEnum.Status status = authenticator.sendAuthEmail("user@example.com");
        assertEquals(LoginEnum.Status.AUTH_ALREADY_SENT, status);
    }
    
    @AfterEach
    void cleanUp() {
        // Clean-up
        Authenticator.emailsSent.clear();
    }
}
