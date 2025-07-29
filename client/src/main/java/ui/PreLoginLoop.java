package ui;

import client.ServerFacade;
import exception.ResponseException;
import service.LoginResult;
import service.RegisterResult;

import java.util.Scanner;

public class PreLoginLoop {
    public String run(Scanner scanner, ServerFacade facade) {
        String authToken = null;
        while (true) {
            System.out.print("[Not logged in] > ");
            String input = scanner.nextLine().trim().toLowerCase();
            try {
                switch (input) {
                    case "help" -> printHelp();
                    case "register" -> {
                        authToken = handleRegister(scanner, facade);
                        if (authToken != null) return authToken;
                    }
                    case "login" -> {
                        authToken = handleLogin(scanner, facade);
                        if (authToken != null) return authToken;
                    }
                    case "quit" -> {
                        System.out.println("Goodbye!");
                        return "QUIT";
                    }
                    default -> System.out.println("Unknown command. Type 'help' to see options.");
                }
            } catch (ResponseException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printHelp() {
        System.out.println("Available commands: help, register, login, quit");
    }

    private String handleRegister(Scanner scanner, ServerFacade facade) throws ResponseException {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        RegisterResult result = facade.register(username, password, email);
        System.out.println("Registered and logged in as " + result.username());
        return result.authToken();
    }

    private String handleLogin(Scanner scanner, ServerFacade facade) throws ResponseException {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        LoginResult result = facade.login(username, password);
        System.out.println("Logged in as " + result.username());
        return result.authToken();
    }
}
