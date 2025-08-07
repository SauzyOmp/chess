import chess.*;
import dataaccess.DatabaseManager;
import dataaccess.DataAccessException;
import server.Server;

public class Main {
    public static void main(String[] args) throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.initSchema();
        Server server = new Server();
        server.run(8080);
    }
} 