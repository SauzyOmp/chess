package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;
import chess.ChessGame;

import java.util.*;

public class InMemoryDataAccess implements DataAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<Integer,GameData> games = new HashMap<>();
    private final Map<String,AuthData> auths = new HashMap<>();
    private int gameId = 1;

    @Override
    public void clear() {
        users.clear();
        games.clear();
        auths.clear();
        gameId = 1;
    }

    @Override
    public void createUser(UserData u) throws DataAccessException {
        if (users.containsKey(u.username())) {
            throw new DataAccessException("Username already Taken");
        }
        users.put(u.username(), u);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData u = users.get(username);
        if (u == null) {
            throw new DataAccessException("User not found");
        }
        return u;
    }

    @Override
    public GameData createGame(String gameName) {
        int id = gameId++;
        GameData g = new GameData(id, null, null, gameName, new ChessGame());
        games.put(id, g);
        return g;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData g = games.get(gameID);
        if (g == null) throw new DataAccessException("Game not found");
        return g;
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData updated) throws DataAccessException {
        if (!games.containsKey(updated.gameID())) {
            throw new DataAccessException("Game not found");
        }
        games.put(updated.gameID(), updated);
    }

    @Override
    public AuthData createAuth(String username) {
        String token = UUID.randomUUID().toString();
        AuthData a = new AuthData(token, username);
        auths.put(token, a);
        return a;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData a = auths.get(authToken);
        if (a == null) throw new DataAccessException("Unauthorized");
        return a;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (auths.remove(authToken) == null) {
            throw new DataAccessException("Unauthorized");
        }
    }
}
