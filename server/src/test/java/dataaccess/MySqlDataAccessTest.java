package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlDataAccessTest {
    private DataAccess dao;

    @BeforeAll
    static void initDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.initSchema();
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        dao = new MySqlDataAccess();
        dao.clear();
    }

    @Test
    void createUserSuccess() throws DataAccessException {
        UserData u = new UserData("reggi", "Pwd123", "reggi@email.com");
        dao.createUser(u);
        // Success if no exception
    }

    @Test
    void createUserDuplicateUsernameThrows() throws DataAccessException {
        UserData u = new UserData("bob", "pw", "bob@email.com");
        dao.createUser(u);
        assertThrows(DataAccessException.class, () -> dao.createUser(u));
    }

    @Test
    void getUserSuccess() throws DataAccessException {
        UserData u = new UserData("alice", "pw", "alice@email.com");
        dao.createUser(u);
        UserData fromDb = dao.getUser("alice");
        assertEquals("alice", fromDb.username());
        assertNotNull(fromDb.password());
        assertFalse(fromDb.password().isEmpty());
        assertEquals("alice@email.com", fromDb.email());
    }

    @Test
    void getUserNonexistentThrows() {
        assertThrows(DataAccessException.class, () -> dao.getUser("no_such_user"));
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        GameData g = dao.createGame("chess-match");
        assertNotNull(g);
        assertTrue(g.gameID() > 0);
    }

    @Test
    void getGameSuccess() throws DataAccessException {
        GameData g = dao.createGame("match");
        GameData fetched = dao.getGame(g.gameID());
        assertEquals(g.gameID(), fetched.gameID());
        assertEquals("match", fetched.gameName());
        assertNotNull(fetched.game());
    }

    @Test
    void getGameNonexistentThrows() {
        assertThrows(DataAccessException.class, () -> dao.getGame(99999));
    }

    @Test
    void listGamesReturnsAll() throws DataAccessException {
        dao.createGame("g1");
        dao.createGame("g2");
        List<GameData> games = dao.listGames();
        assertEquals(2, games.size());
    }

    @Test
    void listGamesEmptyReturnsEmptyList() throws DataAccessException {
        List<GameData> games = dao.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
        GameData g = dao.createGame("initial");
        assertDoesNotThrow(() -> dao.updateGame(g));
    }

    @Test
    void updateGameNonexistentThrows() {
        GameData fake = new GameData(99999, null, null, "none", null);
        assertThrows(DataAccessException.class, () -> dao.updateGame(fake));
    }

    @Test
    void createAuthSuccess() throws DataAccessException {
        UserData u = new UserData("authuser", "pw", "user@email.com");
        dao.createUser(u);
        AuthData auth = dao.createAuth("authuser");
        assertNotNull(auth.authToken());
        assertEquals("authuser", auth.username());
    }

    @Test
    void getAuthSuccess() throws DataAccessException {
        UserData u = new UserData("authuser2", "pw", "user2@email.com");
        dao.createUser(u);
        AuthData auth = dao.createAuth("authuser2");
        AuthData fetched = dao.getAuth(auth.authToken());
        assertEquals(auth.authToken(), fetched.authToken());
        assertEquals("authuser2", fetched.username());
    }

    @Test
    void getAuthNonexistentThrows() {
        assertThrows(DataAccessException.class, () -> dao.getAuth("no-such-token"));
    }

    @Test
    void deleteAuthSuccess() throws DataAccessException {
        UserData u = new UserData("authuser3", "pw", "user3@email.com");
        dao.createUser(u);
        AuthData auth = dao.createAuth("authuser3");
        assertDoesNotThrow(() -> dao.deleteAuth(auth.authToken()));
    }

    @Test
    void deleteAuthNonexistentThrows() {
        assertThrows(DataAccessException.class, () -> dao.deleteAuth("no-such-token"));
    }

    @Test
    void clearSuccess() throws DataAccessException {
        UserData u = new UserData("clearuser", "pw", "clear@email.com");
        dao.createUser(u);
        dao.clear();
        assertThrows(DataAccessException.class, () -> dao.getUser("clearuser"));
    }

    @Test
    void createGameInvalidInputThrows() {
        assertThrows(DataAccessException.class, () -> dao.createGame(null));
    }

    @Test
    void createAuthNonexistentUserThrows() {
        assertThrows(DataAccessException.class, () -> dao.createAuth("no_such_user"));
    }
}
