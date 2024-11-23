package ppp.auth;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import ppp.db.UserRepository;
import ppp.db.model.OUser;
import ppp.meta.LoginEnum;

class AuthenticatorTest {

    private UserRepository userRepositoryMock;
    private Authenticator authenticator;

    @BeforeEach
    void setUp() {
        userRepositoryMock = mock(UserRepository.class);
        authenticator = new Authenticator(userRepositoryMock);
    }

    @Test
    void testLoginSuccess() {
        OUser mockUser = new OUser();
        mockUser.id = 1;
        mockUser.email = "user@example.com";
        mockUser.token = "validToken";
        mockUser.tokenExpiryDate = new Timestamp(System.currentTimeMillis() + 10000);
        mockUser.banned = false;

        when(userRepositoryMock.findByEmail("user@example.com", true)).thenReturn(mockUser);

        LoginEnum.Status status = authenticator.login("user@example.com", "validToken");
        assertEquals(LoginEnum.Status.SUCCESS, status);

        verify(userRepositoryMock).update(any(OUser.class));
    }

    @Test
    void testLoginUserInvalid() {
        when(userRepositoryMock.findByEmail("invalid@example.com", true)).thenReturn(new OUser());

        LoginEnum.Status status = authenticator.login("invalid@example.com", "anyToken");
        assertEquals(LoginEnum.Status.USER_INVALID, status);
    }

    @Test
    void testLoginTokenExpired() {
        OUser mockUser = new OUser();
        mockUser.id = 1;
        mockUser.email = "user@example.com";
        mockUser.token = "expiredToken";
        mockUser.tokenExpiryDate = new Timestamp(System.currentTimeMillis() - 10000);
        mockUser.banned = false;

        when(userRepositoryMock.findByEmail("user@example.com", true)).thenReturn(mockUser);

        LoginEnum.Status status = authenticator.login("user@example.com", "expiredToken");
        assertEquals(LoginEnum.Status.TOKEN_EXPIRED, status);
    }
}
