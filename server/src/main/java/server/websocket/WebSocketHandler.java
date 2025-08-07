package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import chess.ChessPosition;

@WebSocket
public class WebSocketHandler {
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();
    private final DataAccess dataAccess;
    private final Gson gson = new Gson();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            String authToken = command.getAuthToken();
            Integer gameID = command.getGameID();

            AuthData authData = dataAccess.getAuth(authToken);
            if (authData == null) {
                sendError(session, "Error: Invalid auth token");
                return;
            }

            GameData gameData = dataAccess.getGame(gameID);
            if (gameData == null) {
                sendError(session, "Error: Game not found");
                return;
            }

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, authData, gameData);
                case MAKE_MOVE -> handleMakeMove(session, message, authData, gameData);
                case LEAVE -> handleLeave(session, authData, gameData);
                case RESIGN -> handleResign(session, authData, gameData);
            }
        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void handleConnect(Session session, AuthData authData, GameData gameData) throws IOException {
        String username = authData.username();
        String gameKey = String.valueOf(gameData.gameID());
        
        Connection connection = new Connection(username, session, gameData);
        connections.put(username, connection);

        sendLoadGame(session, gameData.game());
        
        String notification = username + " joined the game as an observer";
        if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username)) {
            notification = username + " joined the game as WHITE";
        } else if (gameData.blackUsername() != null && gameData.blackUsername().equals(username)) {
            notification = username + " joined the game as BLACK";
        }
        
        broadcastToOthers(username, gameKey, new NotificationMessage(notification));
    }

    private void handleMakeMove(Session session, String message, AuthData authData, GameData gameData) throws IOException {
        String username = authData.username();
        String gameKey = String.valueOf(gameData.gameID());
        
        if (!isPlayer(username, gameData)) {
            sendError(session, "Error: Only players can make moves");
            return;
        }

        if (!isPlayerTurn(username, gameData)) {
            sendError(session, "Error: Not your turn");
            return;
        }

        if (isGameOver(gameData)) {
            sendError(session, "Error: Game is over");
            return;
        }

        MakeMoveCommand moveCommand = gson.fromJson(message, MakeMoveCommand.class);
        ChessMove move = moveCommand.getMove();

        try {
            gameData.game().makeMove(move);
            try {
                dataAccess.updateGame(gameData);
                gameData = dataAccess.getGame(gameData.gameID());
            } catch (DataAccessException e) {
                sendError(session, "Error: Failed to update game");
                return;
            }
            
            if (!isGameOver(gameData)) {
                broadcastToAll(gameKey, new LoadGameMessage(gameData.game()));
            }
            
            String moveDescription = describeMove(move);
            String notification = username + " made move: " + moveDescription;
            broadcastToOthers(username, gameKey, new NotificationMessage(notification));
            
            if (gameData.game().isInCheckmate(gameData.game().getTeamTurn())) {
                ChessGame.TeamColor winningColor = gameData.game().getTeamTurn() == ChessGame.TeamColor.WHITE ? 
                    ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                String winner = getPlayerUsername(gameData, winningColor);
                String loser = getPlayerUsername(gameData, gameData.game().getTeamTurn());
                String checkmateNotification = "Checkmate! " + winner + " wins! Congratulations!";
                broadcastToAll(gameKey, new NotificationMessage(checkmateNotification));
            } else if (gameData.game().isInCheck(gameData.game().getTeamTurn())) {
                String checkNotification = getPlayerUsername(gameData, gameData.game().getTeamTurn()) + " is in check";
                broadcastToAll(gameKey, new NotificationMessage(checkNotification));
            } else if (gameData.game().isInStalemate(gameData.game().getTeamTurn())) {
                String stalemateNotification = "Game ended in stalemate - it's a draw!";
                broadcastToAll(gameKey, new NotificationMessage(stalemateNotification));
            }
        } catch (InvalidMoveException e) {
            sendError(session, "Error: Invalid move");
            return;
        }
    }

    private void handleLeave(Session session, AuthData authData, GameData gameData) throws IOException {
        String username = authData.username();
        String gameKey = String.valueOf(gameData.gameID());
        
        connections.remove(username);
        
        if (isPlayer(username, gameData)) {
            GameData updatedGame = removePlayer(username, gameData);
            try {
                dataAccess.updateGame(updatedGame);
                gameData = dataAccess.getGame(gameData.gameID());
            } catch (DataAccessException e) {
                sendError(session, "Error: Failed to update game");
                return;
            }
        }
        
        String notification = username + " left the game";
        broadcastToOthers(username, gameKey, new NotificationMessage(notification));
    }

    private void handleResign(Session session, AuthData authData, GameData gameData) throws IOException {
        String username = authData.username();
        String gameKey = String.valueOf(gameData.gameID());
        
        if (!isPlayer(username, gameData)) {
            sendError(session, "Error: Only players can resign");
            return;
        }

        if (isGameOver(gameData)) {
            sendError(session, "Error: Game is already over");
            return;
        }

        // Set the game as over by setting team turn to null
        gameData.game().setTeamTurn(null);
        try {
            dataAccess.updateGame(gameData);
            gameData = dataAccess.getGame(gameData.gameID());
        } catch (DataAccessException e) {
            sendError(session, "Error: Failed to update game");
            return;
        }

        // Determine the winner (the player who didn't resign)
        String winner = null;
        if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username)) {
            winner = gameData.blackUsername();
        } else if (gameData.blackUsername() != null && gameData.blackUsername().equals(username)) {
            winner = gameData.whiteUsername();
        }

        String resignationNotification = username + " resigned. " + winner + " wins!";
        broadcastToAll(gameKey, new NotificationMessage(resignationNotification));
    }

    private boolean isGameOver(GameData gameData) {
        return gameData.game().getTeamTurn() == null;
    }

    private boolean isPlayer(String username, GameData gameData) {
        return (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username)) ||
               (gameData.blackUsername() != null && gameData.blackUsername().equals(username));
    }

    private boolean isPlayerTurn(String username, GameData gameData) {
        ChessGame.TeamColor playerColor = null;
        if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username)) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (gameData.blackUsername() != null && gameData.blackUsername().equals(username)) {
            playerColor = ChessGame.TeamColor.BLACK;
        }
        return playerColor == gameData.game().getTeamTurn();
    }

    private GameData removePlayer(String username, GameData gameData) {
        if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username)) {
            return new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else if (gameData.blackUsername() != null && gameData.blackUsername().equals(username)) {
            return new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
        }
        return gameData;
    }

    private String describeMove(ChessMove move) {
        String startPos = formatPosition(move.getStartPosition());
        String endPos = formatPosition(move.getEndPosition());
        return startPos + " to " + endPos;
    }
    
    private String formatPosition(ChessPosition position) {
        char col = (char)('a' + position.getColumn() - 1);
        return col + String.valueOf(position.getRow());
    }

    private void sendLoadGame(Session session, ChessGame game) throws IOException {
        session.getRemote().sendString(gson.toJson(new LoadGameMessage(game)));
    }

    private void sendError(Session session, String errorMessage) throws IOException {
        session.getRemote().sendString(gson.toJson(new ErrorMessage(errorMessage)));
    }

    private void broadcastToOthers(String excludeUsername, String gameKey, ServerMessage message) throws IOException {
        for (Connection connection : connections.values()) {
            if (!connection.username().equals(excludeUsername) && 
                String.valueOf(connection.gameData().gameID()).equals(gameKey)) {
                connection.session().getRemote().sendString(gson.toJson(message));
            }
        }
    }

    private void broadcastToAll(String gameKey, ServerMessage message) throws IOException {
        for (Connection connection : connections.values()) {
            if (String.valueOf(connection.gameData().gameID()).equals(gameKey)) {
                connection.session().getRemote().sendString(gson.toJson(message));
            }
        }
    }

    private String getPlayerUsername(GameData gameData, ChessGame.TeamColor teamColor) {
        if (teamColor == ChessGame.TeamColor.WHITE) {
            return gameData.whiteUsername();
        } else if (teamColor == ChessGame.TeamColor.BLACK) {
            return gameData.blackUsername();
        }
        return null; // Should not happen if teamColor is valid
    }

    private record Connection(String username, Session session, GameData gameData) {}
} 