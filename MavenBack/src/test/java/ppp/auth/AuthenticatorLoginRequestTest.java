package ppp.auth;

import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ppp.db.UserRepository;
import ppp.db.model.OUser;
import ppp.meta.LoginEnum;

class AuthenticatorLoginRequestTest {

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    
    @BeforeEach
    void setUp() {
    	request = mock(HttpServletRequest.class);
    	session = mock(HttpSession.class);
    	when(request.getSession()).thenReturn(session); // Return mocked session
    }
	
    @Test
    void testLoginRequestSuccess() {
        when(request.getSession().getAttribute("email")).thenReturn("user@stevens.edu");
        when(request.getSession().getAttribute("token")).thenReturn("validToken");

        UserRepository userRepository = mock(UserRepository.class);
        OUser mockUser = new OUser();
        mockUser.id = 1;
        mockUser.email = "user@stevens.edu";
        mockUser.token = "validToken";
        mockUser.tokenExpiryDate = new Timestamp(new Date(System.currentTimeMillis() + 10000).getTime());
        when(userRepository.findByEmail("user@stevens.edu", true)).thenReturn(mockUser);

        Authenticator authenticator = new Authenticator(userRepository);
        LoginEnum.Status status = authenticator.login(request);

        assertEquals(LoginEnum.Status.SUCCESS, status);
    }

    @Test
    void testLoginRequestEmailInvalid() {
        when(request.getSession().getAttribute("email")).thenReturn("user@gmail.com");

        Authenticator authenticator = new Authenticator(null); // No repository needed for this test.
        LoginEnum.Status status = authenticator.login(request);

        assertEquals(LoginEnum.Status.EMAIL_INVALID, status);
    }

    @Test
    void testLoginRequestTokenInvalid() {
        when(request.getSession().getAttribute("email")).thenReturn("user@stevens.edu");
        when(request.getSession().getAttribute("token")).thenReturn("");

        Authenticator authenticator = new Authenticator(null); // No repository needed for this test.
        LoginEnum.Status status = authenticator.login(request);

        assertEquals(LoginEnum.Status.TOKEN_INVALID, status);
    }

    @Test
    void testLoginRequestNullEmail() {
        when(request.getSession().getAttribute("email")).thenReturn(null);

        Authenticator authenticator = new Authenticator(null); // No repository needed for this test.
        LoginEnum.Status status = authenticator.login(request);

        assertEquals(LoginEnum.Status.EMAIL_INVALID, status);
    }

    @Test
    void testLoginRequestNullToken() {
        when(request.getSession().getAttribute("email")).thenReturn("user@stevens.edu");
        when(request.getSession().getAttribute("token")).thenReturn(null);

        Authenticator authenticator = new Authenticator(null); // No repository needed for this test.
        LoginEnum.Status status = authenticator.login(request);

        assertEquals(LoginEnum.Status.TOKEN_INVALID, status);
    }
}