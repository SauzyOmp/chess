package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;
import java.sql.SQLException;
import java.util.List;

/**
 * MySQL-backed implementation of DataAccess.
 */
public class MySqlDataAccess implements DataAccess {

    @Override
    public void clear() throws DataAccessException {
        // (example of implemented method)
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE Auths");
            stmt.execute("TRUNCATE TABLE Games");
            stmt.execute("TRUNCATE TABLE Users");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear tables", e);
        }
    }

    @Override
    public void createUser(UserData u) throws DataAccessException {
        throw new UnsupportedOperationException("createUser not implemented yet");
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        throw new UnsupportedOperationException("getUser not implemented yet");
    }

    @Override
    public GameData createGame(String gameName) {
        throw new UnsupportedOperationException("createGame not implemented yet");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        throw new UnsupportedOperationException("getGame not implemented yet");
    }

    @Override
    public List<GameData> listGames() {
        throw new UnsupportedOperationException("listGames not implemented yet");
    }

    @Override
    public void updateGame(GameData updated) throws DataAccessException {
        throw new UnsupportedOperationException("updateGame not implemented yet");
    }

    @Override
    public AuthData createAuth(String username) {
        throw new UnsupportedOperationException("createAuth not implemented yet");
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        throw new UnsupportedOperationException("getAuth not implemented yet");
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        throw new UnsupportedOperationException("deleteAuth not implemented yet");
    }
}
