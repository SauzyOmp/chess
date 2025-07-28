package client;

import org.junit.jupiter.api.*;
import server.Server;
import service.CreateGameResult;
import service.GamesResult;
import service.LoginResult;
import service.RegisterResult;
import client.ServerFacade;
import exception.ResponseException;
import model.GameData;


public class ServerFacadeTests {
    private static Server server;
    private static ServerFacade facade;
    private static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade(port);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clearDatabase();
    }

    @Test
    public void registerPositive() throws Exception {
        RegisterResult result = facade.register("user1", "pass1", "reggie1@example.com");
        Assertions.assertNotNull(result.authToken());
        Assertions.assertFalse(result.authToken().isEmpty());
        Assertions.assertEquals("user1", result.username());
    }

    @Test
    public void registerNegative() throws Exception {
        facade.register("user2", "pass2", "reggie2@example.com");
        Assertions.assertThrows(ResponseException.class, () -> facade.register("user2", "passX", "reggie2b@example.com"));
    }

    @Test
    public void loginPositive() throws Exception {
        facade.register("user3", "pass3", "reggie3@example.com");
        LoginResult result = facade.login("user3", "pass3");
        Assertions.assertNotNull(result.authToken());
        Assertions.assertFalse(result.authToken().isEmpty());
        Assertions.assertEquals("user3", result.username());
    }

    @Test
    public void loginNegative() throws Exception {
        facade.register("user4", "pass4", "reggie4@example.com");
        Assertions.assertThrows(ResponseException.class, () -> facade.login("user4", "wrong"));
    }

    @Test
    public void listGamesPositive() throws Exception {
        facade.register("user6", "pass6", "reggie6@example.com");
        LoginResult login = facade.login("user6", "pass6");
        GamesResult games = facade.listGames(login.authToken());
        Assertions.assertNotNull(games);
        Assertions.assertTrue(games.games().isEmpty());
    }

    @Test
    public void listGamesNegative() throws Exception {
        facade.register("user5", "pass5", "reggie5@example.com");
        LoginResult login = facade.login("user5", "pass5");
        facade.logout(login.authToken());
        Assertions.assertThrows(ResponseException.class, () -> facade.listGames(login.authToken()));
    }

    @Test
    public void createGamePositive() throws Exception {
        facade.register("user7", "pass7", "reggie7@example.com");
        LoginResult login = facade.login("user7", "pass7");
        CreateGameResult created = facade.createGame(login.authToken(), "myGame");
        Assertions.assertNotNull(created.gameID());
        Assertions.assertNotNull(created.gameID());
        GamesResult after = facade.listGames(login.authToken());
        Assertions.assertEquals(1, after.games().size());
    }

    @Test
    public void createGameNegative() {
        Assertions.assertThrows(ResponseException.class, () -> facade.createGame("badToken", "x"));
    }

    @Test
    public void joinGamePositive() throws Exception {
        facade.register("user8", "pass8", "reggie8@example.com");
        LoginResult ownerLogin = facade.login("user8", "pass8");
        CreateGameResult created = facade.createGame(ownerLogin.authToken(), "g8");
        facade.register("user9", "pass9", "reggie9@example.com");
        LoginResult joinLogin = facade.login("user9", "pass9");
        facade.joinGame(joinLogin.authToken(), String.valueOf(created.gameID()), "WHITE");
    }

    @Test
    public void joinGameNegative() throws Exception {
        facade.register("user10", "pass10", "reggie10@example.com");
        LoginResult login = facade.login("user10", "pass10");
        Assertions.assertThrows(ResponseException.class, () -> facade.joinGame(login.authToken(), "9999", "BLACK"));
    }
}
