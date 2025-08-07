package client;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketFacade {
    private Session session;
    private final Gson gson = new Gson();
    private GameHandler gameHandler;

    public WebSocketFacade(String url, GameHandler gameHandler) throws Exception {
        this.gameHandler = gameHandler;
        URI uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
        switch (serverMessage.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage loadGameMessage = gson.fromJson(message, LoadGameMessage.class);
                gameHandler.updateGame(loadGameMessage.getGame());
            }
            case ERROR -> {
                ErrorMessage errorMessage = gson.fromJson(message, ErrorMessage.class);
                gameHandler.printMessage(errorMessage.getErrorMessage());
            }
            case NOTIFICATION -> {
                NotificationMessage notificationMessage = gson.fromJson(message, NotificationMessage.class);
                gameHandler.printMessage(notificationMessage.getMessage());
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        // WebSocket connection closed - notify the game handler
        gameHandler.printMessage("WebSocket connection closed: " + closeReason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // WebSocket error occurred - notify the game handler
        gameHandler.printMessage("WebSocket error: " + throwable.getMessage());
    }

    public void connect(String authToken, Integer gameID) throws IOException {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move) throws IOException {
        MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void leave(String authToken, Integer gameID) throws IOException {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void resign(String authToken, Integer gameID) throws IOException {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void close() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                gameHandler.printMessage("Error closing WebSocket: " + e.getMessage());
            }
        }
    }

    public interface GameHandler {
        void updateGame(ChessGame game);
        void printMessage(String message);
    }
} 