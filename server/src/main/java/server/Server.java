package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MySqlDataAccess;
import dataaccess.DatabaseManager;
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
    private final DataAccess dao = new MySqlDataAccess();

    // Refactor the run method by moving endpoint setup and initialization into private helper methods to reduce method length.

    public int run(int desiredPort) {
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.initSchema();
        } catch (Exception e) {
            System.err.println("Failed to create database: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
                var result = new UserService(dao).register(r);
                res.status(200);
                return gson.toJson(result);
            } catch (JsonSyntaxException e) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            } catch (DataAccessException e) {
                String msg = e.getMessage().toLowerCase();
                if (msg.contains("taken")) {
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
                LoginResult result = new UserService(dao).login(r);
                res.status(200);
                return gson.toJson(result);
            } catch (JsonSyntaxException e) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            } catch (DataAccessException e) {
                String msg = e.getMessage();
                if (msg != null) {
                    String trimmed = msg.trim().toLowerCase();
                    if (trimmed.equals("unauthorized") || trimmed.equals("user not found") || trimmed.equals("username/password incorrect")) {
                        res.status(401);
                    } else {
                        res.status(500);
                    }
                } else {
                    res.status(500);
                }
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        });

        delete("/session", (req, res) -> {
            String token = req.headers("Authorization");
            try {
                new UserService(dao).logout(token);
                res.status(200);
                return "{}";
            } catch (DataAccessException e) {
                String msg = e.getMessage();
                if (msg != null) {
                    String trimmed = msg.trim().toLowerCase();
                    if (trimmed.equals("unauthorized") || trimmed.equals("user not found")) {
                        res.status(401);
                    } else {
                        res.status(500);
                    }
                } else {
                    res.status(500);
                }
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        });

        get("/game", (req, res) -> {
            try {
                String token = req.headers("Authorization");
                GamesResult games = new GameService(dao).listGames(token);
                res.status(200);
                return gson.toJson(games);
            } catch (DataAccessException e) {
                String msg = e.getMessage();
                if (msg != null && msg.trim().equalsIgnoreCase("unauthorized")) {
                    res.status(401);
                } else if (msg != null && msg.toLowerCase().contains("not found")) {
                    res.status(400);
                } else {
                    res.status(500);
                }
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
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
                CreateGameResult result = new GameService(dao).createGame(token, r);
                res.status(200);
                return gson.toJson(result);
            } catch (JsonSyntaxException e) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            } catch (DataAccessException e) {
                String msg = e.getMessage();
                if (msg != null && msg.trim().equalsIgnoreCase("unauthorized")) {
                    res.status(401);
                } else {
                    res.status(500);
                }
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        });

        put("/game", (req, res) -> {
            try {
                String token = req.headers("Authorization");
                JoinGameRequest r = gson.fromJson(req.body(), JoinGameRequest.class);
                if (r.playerColor() == null ||
                        !(r.playerColor().equals("WHITE") || r.playerColor().equals("BLACK"))) {
                    res.status(400);
                    return gson.toJson(Map.of("message", "Error: bad request"));
                }
                new GameService(dao).joinGame(token, r);
                res.status(200);
                return "{}";
            } catch (JsonSyntaxException e) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            } catch (DataAccessException e) {
                String msg = e.getMessage();
                if (msg != null && msg.trim().equalsIgnoreCase("unauthorized")) {
                    res.status(401);
                } else if (msg != null && (msg.toLowerCase().contains("taken") || msg.toLowerCase().contains("full"))) {
                    res.status(403);
                } else if (msg != null && msg.toLowerCase().contains("not found")) {
                    res.status(400);
                } else {
                    res.status(500);
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

    public static void main(String[] args) {
        new Server().run(4567);
    }
}
