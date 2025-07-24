package service;

import dataaccess.InMemoryDataAccess;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    @Test
    public void registerSuccess() throws DataAccessException {
        var dao = new InMemoryDataAccess();
        var service = new UserService(dao);
        var req = new RegisterRequest("Reginald the 1st", "pass123", "reggie@realemail.com");

        var result = service.register(req);

        assertEquals("Reginald the 1st",result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void registerDuplicate() throws DataAccessException {
        var dao = new InMemoryDataAccess();
        var service = new UserService(dao);
        var req = new RegisterRequest("bob the BUILDER", "pw", "bob@veryrealEmail.com");

        service.register(req);
        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> service.register(req)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("taken"));
    }

    @Test
    public void loginSuccess() throws DataAccessException {
        var dao = new InMemoryDataAccess();
        var service = new UserService(dao);
        service.register(new RegisterRequest("Reginald the 2nd", "mypw", "reggieforeal@email.com"));

        var loginReq = new LoginRequest("Reginald the 2nd", "mypw");
        var loginRes = service.login(loginReq);

        assertEquals("Reginald the 2nd", loginRes.username());
        assertNotNull( loginRes.authToken());
    }

    @Test
    public void loginBadPassword() throws DataAccessException {
        var dao = new InMemoryDataAccess();
        var service = new UserService(dao);
        service.register(new RegisterRequest("Reginald the 3rd", "secretysecret", "reggiedabomb@email.com"));

        var badReq = new LoginRequest("Reginald the 3rd","wrongpw");
        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> service.login(badReq)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("incorrect"));
    }

    @Test
    public void logoutSuccess() throws DataAccessException {
        var dao = new InMemoryDataAccess();
        var service = new UserService(dao);

        service.register(new RegisterRequest("Reg the 17th", "pw", "reg1717@emails.com"));
        var loginRes = service.login(new LoginRequest("Reg the 17th", "pw"));

        service.logout(loginRes.authToken());

        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> dao.getAuth(loginRes.authToken())
        );
        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    public void logoutInvalidToken() {
        var dao = new InMemoryDataAccess();
        var service = new UserService(dao);

        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> service.logout("nonexistent-token")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }
}
