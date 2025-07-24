package service;

import dataaccess.InMemoryDataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {

    @Test
    public void clearSuccess() throws DataAccessException {
        var dao = new InMemoryDataAccess();
        var userService = new UserService(dao);
        var gameService = new GameService(dao);

        var reg1 = userService.register(
                new RegisterRequest("Reginald I", "pw1", "r1@emails.com")
        );
        gameService.createGame(reg1.authToken(), new CreateGameRequest("Reginald's Arena"));
        gameService.clear();
        var king = userService.register(
                new RegisterRequest("King Reginald", "royalPW", "king@kinglyemails.com")
        );
        List<GameData> games = gameService.listGames(king.authToken()).games();
        assertTrue(games.isEmpty());
    }

    @Test
    public void listGamesUnauthorized() {
        var dao = new InMemoryDataAccess();
        var gameService = new GameService(dao);
        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> gameService.listGames("bad-token")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    public void listGamesSuccess() throws DataAccessException {
        var dao = new InMemoryDataAccess();
        var userService = new UserService(dao);
        var gameService = new GameService(dao);
        var reg = userService.register(new RegisterRequest("Reginald II", "pw2", "r2@emails4real.com"));
        gameService.createGame(reg.authToken(), new CreateGameRequest("Reginald's Match"));
        List<GameData> games = gameService.listGames(reg.authToken()).games();
        assertEquals(1, games.size());
        assertEquals("Reginald's Match", games.get(0).gameName());
    }

    @Test
    public void createGameUnauthorized() {
        var dao = new InMemoryDataAccess();
        var gameService = new GameService(dao);
        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> gameService.createGame("bad-token", new CreateGameRequest("Reginald's Quest"))
        );
        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        var dao = new InMemoryDataAccess();
        var userService = new UserService(dao);
        var gameService = new GameService(dao);
        var reg = userService.register(new RegisterRequest("Reginald III", "pw3", "r3@esnail.com"));
        var result = gameService.createGame(reg.authToken(), new CreateGameRequest("Reginald's Challenge"));
        assertTrue(result.gameID() > 0);
    }

    @Test
    public void joinGameUnauthorized() {
        var dao = new InMemoryDataAccess();
        var gameService = new GameService(dao);
        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> gameService.joinGame("bad-token", new JoinGameRequest(1, "WHITE"))
        );
        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    public void joinGameSuccess() throws DataAccessException {
        var dao = new InMemoryDataAccess();
        var userService = new UserService(dao);
        var gameService = new GameService(dao);
        var reg1 = userService.register(new RegisterRequest("Reginald IV", "pw4", "r4@ema.com"));
        var gid = gameService.createGame(reg1.authToken(), new CreateGameRequest("Reginald's Duel")).gameID();
        var reg2 = userService.register(new RegisterRequest("Reginald V", "pw5", "r5@em.com"));
        gameService.joinGame(reg2.authToken(), new JoinGameRequest(gid, "BLACK"));
        GameData g = gameService.listGames(reg1.authToken()).games().get(0);
        assertEquals("Reginald V", g.blackUsername());
    }

    @Test
    public void joinGameFull() throws DataAccessException {
        var dao = new InMemoryDataAccess();
        var userService = new UserService(dao);
        var gameService = new GameService(dao);
        var reg1 = userService.register(new RegisterRequest("Reginald VI", "pw6", "r6@emailslll.com"));
        var reg2 = userService.register(new RegisterRequest("Reginald VII", "pw7", "r7@emaillslsi.com"));
        var gid = gameService.createGame(reg1.authToken(), new CreateGameRequest("Reginald's Trial")).gameID();
        gameService.joinGame(reg2.authToken(), new JoinGameRequest(gid, "WHITE"));
        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> gameService.joinGame(reg1.authToken(), new JoinGameRequest(gid, "WHITE"))
        );
        assertTrue(ex.getMessage().toLowerCase().contains("game full"));
    }
}
