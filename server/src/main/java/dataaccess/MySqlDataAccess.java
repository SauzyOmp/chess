package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;
import chess.ChessGame;
import com.google.gson.Gson;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySqlDataAccess implements DataAccess {

    private static final Gson gson = new Gson();

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Auths");
            stmt.execute("DELETE FROM Games");
            stmt.execute("DELETE FROM Users");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to clear tables", ex);
        }
    }

    @Override
    public void createUser(UserData u) throws DataAccessException {
        String sql = "INSERT INTO Users (username, password) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.username());
            ps.setString(2, u.password());
            ps.setString(3, u.email());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create user", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password FROM Users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                } else {
                    throw new DataAccessException("User not found");
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get user", ex);
        }
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        ChessGame game = new ChessGame();
        String stateJson = gson.toJson(game);
        String sql = "INSERT INTO Games (game_name, state_json) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, gameName);
            ps.setString(2, stateJson);
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new GameData(id, null, null, gameName, game);
                } else {
                    throw new DataAccessException("Failed to retrieve generated game ID");
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create game", ex);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT game_name, state_json FROM Games WHERE id = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("game_name");
                    ChessGame game = gson.fromJson(rs.getString("state_json"), ChessGame.class);
                    return new GameData(gameID, null, null, name, game);
                } else {
                    throw new DataAccessException("Game not found");
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get game", ex);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        String sql = "SELECT id, game_name, state_json FROM Games";
        List<GameData> result = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("game_name");
                ChessGame game = gson.fromJson(rs.getString("state_json"), ChessGame.class);
                result.add(new GameData(id, null, null, name, game));
            }
            return result;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to list games", ex);
        }
    }

    @Override
    public void updateGame(GameData updated) throws DataAccessException {
        ChessGame game = updated.game();  // adjust if your getter differs
        String stateJson = gson.toJson(game);
        String sql = "UPDATE Games SET state_json = ? WHERE id = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, stateJson);
            ps.setInt(2, updated.gameID());
            int count = ps.executeUpdate();
            if (count == 0) {
                throw new DataAccessException("Game not found for update");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update game", ex);
        }
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        String sql = "INSERT INTO Auths (token, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setString(2, username);
            ps.executeUpdate();
            return new AuthData(token, username);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create auth", ex);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT username FROM Auths WHERE token = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(authToken, rs.getString("username"));
                } else {
                    throw new DataAccessException("Unauthorized");
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get auth", ex);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM Auths WHERE token = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            int count = ps.executeUpdate();
            if (count == 0) {
                throw new DataAccessException("Unauthorized");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to delete auth", ex);
        }
    }
}
