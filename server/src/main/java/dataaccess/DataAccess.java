package dataaccess;

import Model.AuthData;
import Model.GameData;
import Model.UserData;

import java.util.List;

public interface DataAccess {
    void clear() throws DataAccessException;
    void createUser(UserData u) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    GameData createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void updateGame(GameData updated) throws DataAccessException;
    AuthData createAuth(String username) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
}
