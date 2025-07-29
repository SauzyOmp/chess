package client;

import com.google.gson.Gson;
import exception.ResponseException;
import model.UserData;
import model.AuthData;
import model.GameData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public ServerFacade(String baseUrl) {
        this.serverUrl = baseUrl;
    }

    public void clearDatabase() throws ResponseException {
        makeRequest("DELETE", "/db", null, null, null);
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        UserData req = new UserData(username, password, email);
        return makeRequest("POST", "/user", req, AuthData.class, null);
    }

    public AuthData login(String username, String password) throws ResponseException {
        UserData req = new UserData(username, password, null);
        return makeRequest("POST", "/session", req, AuthData.class, null);
    }

    public void logout(String authToken) throws ResponseException {
        makeRequest("DELETE", "/session", null, null, authToken);
    }

    public GamesResult listGames(String authToken) throws ResponseException {
        return makeRequest("GET", "/game", null, GamesResult.class, authToken);
    }

    public GameResult createGame(String authToken, String gameName) throws ResponseException {
        GameRequest req = new GameRequest(gameName);
        return makeRequest("POST", "/game", req, GameResult.class, authToken);
    }

    public void joinGame(String authToken, String gameId, String playerColor) throws ResponseException {
        JoinRequest req = new JoinRequest(Integer.parseInt(gameId), playerColor);
        makeRequest("PUT", "/game", req, null, authToken);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            URL url = new URI(serverUrl + path).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            if (authToken != null) {
                http.addRequestProperty("Authorization", authToken);
            }
            if (request != null) {
                http.addRequestProperty("Content-Type", "application/json");
                String reqJson = gson.toJson(request);
                try (OutputStream os = http.getOutputStream()) {
                    os.write(reqJson.getBytes());
                }
            }
            http.connect();
            throwIfNotSuccessful(http);

            if (responseClass != null) {
                try (InputStream is = http.getInputStream()) {
                    InputStreamReader reader = new InputStreamReader(is);
                    return gson.fromJson(reader, responseClass);
                }
            }
            return null;
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws java.io.IOException, ResponseException {
        int status = http.getResponseCode();
        if (status / 100 != 2) {
            try (InputStream err = http.getErrorStream()) {
                if (err != null) {
                    throw ResponseException.fromJson(err);
                }
            }
            throw new ResponseException(status, "Unexpected status: " + status);
        }
    }
}
