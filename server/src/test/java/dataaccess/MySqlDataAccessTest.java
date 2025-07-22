package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        UserData u = new UserData("alice", "hashedPwd123", "alice@example.com");
        dao.createUser(u);
        UserData fromDb = dao.getUser("alice");
        assertEquals("alice", fromDb.username());
        assertEquals("hashedPwd123", fromDb.password());
        assertEquals("alice@example.com", fromDb.email());
    }

    @Test
    void createUser_duplicateUsername_throws() throws DataAccessException {
        UserData u = new UserData("bob", "pw", "bob@ex.com");
        dao.createUser(u);
        assertThrows(DataAccessException.class, () -> dao.createUser(u),
                "Inserting a user with duplicate username should fail");
    }

    @Test
    void getUser_nonexistent_throws() {
        assertThrows(DataAccessException.class, () -> dao.getUser("no_such_user"));
    }
}
