package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.InMemoryDataAccess;
import service.LoginRequest;
import service.RegisterRequest;
import service.UserService;
import spark.*;

import java.util.Map;

import static spark.Spark.delete;
import static spark.Spark.post;

public class Server {
    private final Gson gson = new Gson();
    private final DataAccess dao = new InMemoryDataAccess();
    private final UserService userService = new UserService(dao);

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        post("/user", (req, res) -> {
            try {
                RegisterRequest r = gson.fromJson(req.body(), RegisterRequest.class);
                var result = userService.register(r);
                res.status(200);
                return gson.toJson(result);
            } catch (DataAccessException e) {
                res.status( e.getMessage().contains("taken") ? 403 : 400 );
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        });

        post("/session", (req, res) -> {
            try {
                var r = gson.fromJson(req.body(), LoginRequest.class);
                var result = userService.login(r);
                res.status(200);
                return gson.toJson(result);
            } catch (DataAccessException e) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: username/password incorrect"));
            }
        });

        delete("/session", (req, res) -> {
            String token = req.headers("Authorization");
            try {
                userService.logout(token);
                res.status(200);
                return "{}";
            } catch (DataAccessException e) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }
        });


        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
