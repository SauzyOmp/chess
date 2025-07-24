package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.List;

public class GameService {
    private final DataAccess dao;

    public GameService(DataAccess dao) {
        this.dao = dao;
    }

    public void clear() throws DataAccessException {
        dao.clear();
    }

    public GamesResult listGames(String authToken) throws DataAccessException {
        dao.getAuth(authToken);
        List<GameData> all = dao.listGames();
        return new GamesResult(all);
    }

    public CreateGameResult createGame(String authToken,
                                       CreateGameRequest req)
            throws DataAccessException {
        dao.getAuth(authToken);
        GameData created = dao.createGame(req.gameName());
        return new CreateGameResult(created.gameID());
    }

    public void joinGame(String authToken,
                         JoinGameRequest req)
            throws DataAccessException {
        AuthData auth = dao.getAuth(authToken);

        GameData existing = dao.getGame(req.gameID());

        String color = req.playerColor();
        boolean whiteTaken = existing.whiteUsername() != null;
        boolean blackTaken = existing.blackUsername() != null;
        if (("WHITE".equals(color) && whiteTaken) ||
                ("BLACK".equals(color) && blackTaken)) {
            throw new DataAccessException("game full");
        }

        String white = existing.whiteUsername();
        String black = existing.blackUsername();
        if ("WHITE".equals(color)) {
            white = auth.username();
        } else {
            black = auth.username();
        }

        GameData updated = new GameData(
                existing.gameID(),
                white,
                black,
                existing.gameName(),
                existing.game()
        );

        dao.updateGame(updated);
    }
}
