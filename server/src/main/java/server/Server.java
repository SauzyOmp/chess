package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.InMemoryDataAccess;
import service.CreateGameRequest;
import service.CreateGameResult;
import service.GameService;
import service.GamesResult;
import service.JoinGameRequest;
import service.LoginRequest;
import service.LoginResult;
import service.RegisterRequest;
import service.UserService;
import spark.Spark;

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

        delete("/db", (req, res) -> {
            try {
                dao.clear();
                res.status(200);
                return "{}";
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        });

        post("/user", (req, res) -> {
            try {
                RegisterRequest r = gson.fromJson(req.body(), RegisterRequest.class);
                if (r.username() == null || r.password() == null || r.email() == null) {
                    res.status(400);
                    return gson.toJson(Map.of("message", "Error: bad request"));
                }
                var result = userService.register(r);
                res.status(200);
                return gson.toJson(result);
            } catch (JsonSyntaxException e) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            } catch (DataAccessException e) {
                if (e.getMessage().toLowerCase().contains("taken")) {
                    res.status(403);
                } else {
                    res.status(500);
                }
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        });

        post("/session", (req, res) -> {
            try {
                LoginRequest r = gson.fromJson(req.body(), LoginRequest.class);
                if (r.username() == null || r.password() == null) {
                    res.status(400);
                    return gson.toJson(Map.of("message", "Error: bad request"));
                }
                LoginResult result = userService.login(r);
                res.status(200);
                return gson.toJson(result);
            } catch (JsonSyntaxException e) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            } catch (DataAccessException e) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
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
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
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
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        });

        post("/game", (req, res) -> {
            try {
                String token = req.headers("Authorization");
                CreateGameRequest r = gson.fromJson(req.body(), CreateGameRequest.class);
                if (r.gameName() == null) {
                    res.status(400);
                    return gson.toJson(Map.of("message", "Error: bad request"));
                }
                CreateGameResult result = gameService.createGame(token, r);
                res.status(200);
                return gson.toJson(result);
            } catch (JsonSyntaxException e) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            } catch (DataAccessException e) {
                if (e.getMessage().toLowerCase().contains("unauthorized")) {
                    res.status(401);
                } else {
                    res.status(400);
                }
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        });

        put("/game", (req, res) -> {
            try {
                String token = req.headers("Authorization");
                JoinGameRequest r = gson.fromJson(req.body(), JoinGameRequest.class);
                if (r.playerColor() == null
                        || !(r.playerColor().equals("WHITE") || r.playerColor().equals("BLACK"))) {
                    res.status(400);
                    return gson.toJson(Map.of("message", "Error: bad request"));
                }
                gameService.joinGame(token, r);
                res.status(200);
                return "{}";
            } catch (JsonSyntaxException e) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            } catch (DataAccessException e) {
                String msg = e.getMessage().toLowerCase();
                if (msg.contains("unauthorized")) {
                    res.status(401);
                } else if (msg.contains("taken") || msg.contains("full")) {
                    res.status(403);
                } else {
                    res.status(400);
                }
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        });

        awaitInitialization();
        return port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
