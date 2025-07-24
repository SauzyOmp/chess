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
    void createUser_success() throws DataAccessException {
        UserData u = new UserData("reggi", "Pwd123", "reggi@email.com");
        dao.createUser(u);
        // Success if no exception
    }

    @Test
    void createUser_duplicateUsername_throws() throws DataAccessException {
        UserData u = new UserData("bob", "pw", "bob@email.com");
        dao.createUser(u);
        assertThrows(DataAccessException.class, () -> dao.createUser(u));
    }

    @Test
    void getUser_success() throws DataAccessException {
        UserData u = new UserData("alice", "pw", "alice@email.com");
        dao.createUser(u);
        UserData fromDb = dao.getUser("alice");
        assertEquals("alice", fromDb.username());
        assertNotNull(fromDb.password());
        assertFalse(fromDb.password().isEmpty());
        assertEquals("alice@email.com", fromDb.email());
    }

    @Test
    void getUser_nonexistent_throws() {
        assertThrows(DataAccessException.class, () -> dao.getUser("no_such_user"));
    }

    @Test
    void createGame_success() throws DataAccessException {
        GameData g = dao.createGame("chess-match");
        assertNotNull(g);
        assertTrue(g.gameID() > 0);
    }

    @Test
    void getGame_success() throws DataAccessException {
        GameData g = dao.createGame("match");
        GameData fetched = dao.getGame(g.gameID());
        assertEquals(g.gameID(), fetched.gameID());
        assertEquals("match", fetched.gameName());
        assertNotNull(fetched.game());
    }

    @Test
    void getGame_nonexistent_throws() {
        assertThrows(DataAccessException.class, () -> dao.getGame(99999));
    }

    @Test
    void listGames_returnsAll() throws DataAccessException {
        dao.createGame("g1");
        dao.createGame("g2");
        List<GameData> games = dao.listGames();
        assertEquals(2, games.size());
    }

    @Test
    void listGames_empty_returnsEmptyList() throws DataAccessException {
        List<GameData> games = dao.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void updateGame_success() throws DataAccessException {
        GameData g = dao.createGame("initial");
        assertDoesNotThrow(() -> dao.updateGame(g));
    }

    @Test
    void updateGame_nonexistent_throws() {
        GameData fake = new GameData(99999, null, null, "none", null);
        assertThrows(DataAccessException.class, () -> dao.updateGame(fake));
    }

    @Test
    void createAuth_success() throws DataAccessException {
        UserData u = new UserData("authuser", "pw", "user@email.com");
        dao.createUser(u);
        AuthData auth = dao.createAuth("authuser");
        assertNotNull(auth.authToken());
        assertEquals("authuser", auth.username());
    }

    @Test
    void getAuth_success() throws DataAccessException {
        UserData u = new UserData("authuser2", "pw", "user2@email.com");
        dao.createUser(u);
        AuthData auth = dao.createAuth("authuser2");
        AuthData fetched = dao.getAuth(auth.authToken());
        assertEquals(auth.authToken(), fetched.authToken());
        assertEquals("authuser2", fetched.username());
    }

    @Test
    void getAuth_nonexistent_throws() {
        assertThrows(DataAccessException.class, () -> dao.getAuth("no-such-token"));
    }

    @Test
    void deleteAuth_success() throws DataAccessException {
        UserData u = new UserData("authuser3", "pw", "user3@email.com");
        dao.createUser(u);
        AuthData auth = dao.createAuth("authuser3");
        assertDoesNotThrow(() -> dao.deleteAuth(auth.authToken()));
    }

    @Test
    void deleteAuth_nonexistent_throws() {
        assertThrows(DataAccessException.class, () -> dao.deleteAuth("no-such-token"));
    }
}
