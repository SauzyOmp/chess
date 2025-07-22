package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
