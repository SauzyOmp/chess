package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.InMemoryDataAccess;
import service.*;

import java.util.Map;

import static spark.Spark.*;

public class Server {
    private final Gson gson = new Gson();
    private final DataAccess dao = new InMemoryDataAccess();
    private final UserService userService = new UserService(dao);
    private final GameService gameService = new GameService(dao);

    public int run(int desiredPort) {
        port(desiredPort);
        staticFiles.location("web");

        before((req, res) -> res.type("application/json"));

        post("/user", (req, res) -> {
            try {
                RegisterRequest r = gson.fromJson(req.body(), RegisterRequest.class);
                var result = userService.register(r);
                res.status(200);
                return gson.toJson(result);
            } catch (DataAccessException e) {
                res.status(e.getMessage().contains("taken") ? 403 : 400);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        });

        post("/session", (req, res) -> {
            try {
                LoginRequest r = gson.fromJson(req.body(), LoginRequest.class);
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

        get("/game", (req, res) -> {
            try {
                String token = req.headers("Authorization");
                GamesResult games = gameService.listGames(token);
                res.status(200);
                return gson.toJson(games);
            } catch (DataAccessException e) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        });

        post("/game", (req, res) -> {
            try {
                String token = req.headers("Authorization");
                CreateGameRequest createReq = gson.fromJson(req.body(), CreateGameRequest.class);
                CreateGameResult createRes = gameService.createGame(token, createReq);
                res.status(200);
                return gson.toJson(createRes);
            } catch (DataAccessException e) {
                res.status(e.getMessage().toLowerCase().contains("unauthorized") ? 401 : 400);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }
        });

        put("/game", (req, res) -> {
            try {
                String token = req.headers("Authorization");
                JoinGameRequest joinReq = gson.fromJson(req.body(), JoinGameRequest.class);
                gameService.joinGame(token, joinReq);
                res.status(200);
                return "{}";
            } catch (DataAccessException e) {
                String msg = e.getMessage().toLowerCase();
                int code = msg.contains("unauthorized") ? 401 : msg.contains("game full") ? 403 : 400;
                res.status(code);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }
        });

        awaitInitialization();
        return port();
    }

    public void stop() {
        stop();
        awaitStop();
    }
}
