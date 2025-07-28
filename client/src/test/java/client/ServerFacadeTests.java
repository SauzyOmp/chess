package client;

import org.junit.jupiter.api.*;
import server.Server;
import service.LoginResult;
import service.RegisterResult;
import client.ServerFacade;
import exception.ResponseException;


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

}
