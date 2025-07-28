package client;

import org.junit.jupiter.api.*;
import server.Server;
import service.RegisterResult;
import client.ServerFacade;


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

}
