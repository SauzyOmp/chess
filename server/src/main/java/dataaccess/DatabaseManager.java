package dataaccess;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static final String PROPS_FILE = "db.properties";
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String adminUrl;
    private static String connectionUrl;

    static {
        loadPropertiesFromResources();
    }

    /**
     * Creates the database if it does not already exist, connecting via adminUrl.
     */
    public static void createDatabase() throws DataAccessException {
        String sql = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(adminUrl, dbUsername, dbPassword);
             var stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
        }
    }

    /**
     * Initializes schema: creates necessary tables if they don't exist.
     */
    public static void initSchema() throws DataAccessException {
        String createUsers =
                "CREATE TABLE IF NOT EXISTS Users (" +
                        "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "  username VARCHAR(50) NOT NULL UNIQUE," +
                        "  password CHAR(60) NOT NULL," +
                        "  email VARCHAR(100) NOT NULL UNIQUE," +
                        "  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                        ")";
        String createGames =
                "CREATE TABLE IF NOT EXISTS Games (" +
                        "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "  game_name VARCHAR(100) NOT NULL," +
                        "  state_json TEXT NOT NULL," +
                        "  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                        ")";
        String createAuths =
                "CREATE TABLE IF NOT EXISTS Auths (" +
                        "  token CHAR(36) PRIMARY KEY," +
                        "  username VARCHAR(50) NOT NULL," +
                        "  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "  FOREIGN KEY (username) REFERENCES Users(username)" +
                        ")";

        try (var conn = getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate(createUsers);
            stmt.executeUpdate(createGames);
            stmt.executeUpdate(createAuths);
        } catch (SQLException ex) {
            throw new DataAccessException("failed to init schema", ex);
        }
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            return DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    private static void loadPropertiesFromResources() {
        try (InputStream in = DatabaseManager.class
                .getClassLoader()
                .getResourceAsStream(PROPS_FILE)) {
            if (in == null) throw new RuntimeException(PROPS_FILE + " not found");
            Properties props = new Properties();
            props.load(in);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("unable to process " + PROPS_FILE, ex);
        }
    }

    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");
        String host = props.getProperty("db.host", "localhost");
        String port = props.getProperty("db.port", "3306");

        adminUrl = String.format(
                "jdbc:mysql://%s:%s/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                host, port
        );

        connectionUrl = String.format(
                "jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                host, port, databaseName
        );
    }
}
