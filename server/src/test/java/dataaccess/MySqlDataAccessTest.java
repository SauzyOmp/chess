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
    void createUser_and_getUser_success() throws DataAccessException {
        UserData u = new UserData("reggi", "Pwd123", "reggi@email.com");
        dao.createUser(u);
        UserData fromDb = dao.getUser("reggi");
        assertEquals("reggi", fromDb.username());
        assertNotNull(fromDb.password());
        assertFalse(fromDb.password().isEmpty());
        assertEquals("reggi@email.com", fromDb.email());
    }

    @Test
    void createUser_duplicateUsername_throws() throws DataAccessException {
        UserData u = new UserData("bob", "pw", "bob@email.com");
        dao.createUser(u);
        assertThrows(DataAccessException.class, () -> dao.createUser(u),
                "Inserting a user with duplicate username should fail");
    }

    @Test
    void getUser_nonexistent_throws() {
        assertThrows(DataAccessException.class, () -> dao.getUser("no_such_user"));
    }

    @Test
    void createGame_and_getGame_success() throws DataAccessException {
        GameData g = dao.createGame("chess-match");
        assertNotNull(g);
        assertTrue(g.gameID() > 0);

        GameData fetched = dao.getGame(g.gameID());
        assertEquals(g.gameID(), fetched.gameID());
        assertEquals("chess-match", fetched.gameName());
        assertNotNull(fetched.game());
    }

    @Test
    void getGame_nonexistent_throws() {
        assertThrows(DataAccessException.class, () -> dao.getGame(99999));
    }

    @Test
    void listGames_returnsAllGames() throws DataAccessException {
        dao.createGame("g1");
        dao.createGame("g2");
        List<GameData> games = dao.listGames();
        assertEquals(2, games.size());
        assertTrue(games.stream().anyMatch(g -> g.gameName().equals("g1")));
        assertTrue(games.stream().anyMatch(g -> g.gameName().equals("g2")));
    }

    @Test
    void listGames_empty_returnsEmptyList() throws DataAccessException {
        List<GameData> games = dao.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void updateGame_existingGame_succeeds() throws DataAccessException {
        GameData g = dao.createGame("initial");
        assertDoesNotThrow(() -> dao.updateGame(g));
    }

    @Test
    void updateGame_nonexistentGame_throws() {
        GameData fake = new GameData(99999, null, null, "none", null);
        assertThrows(DataAccessException.class,
                () -> dao.updateGame(fake));
    }

    @Test
    void createAuth_and_getAuth_and_deleteAuth_success() throws DataAccessException {
        UserData u = new UserData("authuser", "pw", "user@email.com");
        dao.createUser(u);

        AuthData auth = dao.createAuth("authuser");
        assertNotNull(auth.authToken());
        assertEquals("authuser", auth.username());

        AuthData fetched = dao.getAuth(auth.authToken());
        assertEquals(auth.authToken(), fetched.authToken());
        assertEquals("authuser", fetched.username());

        assertDoesNotThrow(() -> dao.deleteAuth(auth.authToken()));
        assertThrows(DataAccessException.class,
                () -> dao.getAuth(auth.authToken()),
                "After deleteAuth, getAuth should throw");
    }

    @Test
    void getAuth_nonexistent_throws() {
        assertThrows(DataAccessException.class,
                () -> dao.getAuth("no-such-token"));
    }

    @Test
    void deleteAuth_nonexistent_throws() {
        assertThrows(DataAccessException.class,
                () -> dao.deleteAuth("no-such-token"));
    }
}
