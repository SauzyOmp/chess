package ui;

import client.ServerFacade;
import exception.ResponseException;
import model.AuthData;
import ui.EscapeSequences;

import java.util.Scanner;

public class PreLoginLoop {
    public AuthData run(Scanner scanner, ServerFacade facade) {
        String authToken = null;
        String username = null;
        while (true) {
            System.out.print("[Guest] > ");
            String input = scanner.nextLine().trim().toLowerCase();
            try {
                switch (input) {
                    case "help", "h" -> printHelp();
                    case "register", "r" -> {
                        AuthData result = handleRegister(scanner, facade);
                        if (result != null) {
                            return result;
                        }
                    }
                    case "login", "l" -> {
                        AuthData result = handleLogin(scanner, facade);
                        if (result != null) {
                            return result;
                        }
                    }
                    case "quit", "q" -> {
                        System.out.println("Goodbye!");
                        System.exit(0);
                    }
                    default -> System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + 
                        "Unknown command. Type 'help' to see options." + EscapeSequences.RESET_TEXT_COLOR);
                }
            } catch (ResponseException e) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    private void printHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + EscapeSequences.SET_TEXT_BOLD + 
            "Available commands:" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "help" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (h)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Show this help message");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "register" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (r)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Create a new account");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "login" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (l)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Sign in to your account");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_RED + 
            "quit" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (q)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Exit the program");
    }

    private AuthData handleRegister(Scanner scanner, ServerFacade facade) throws ResponseException {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        AuthData result = facade.register(username, password, email);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Registered and logged in as " + 
            result.username() + EscapeSequences.RESET_TEXT_COLOR);
        return result;
    }

    private AuthData handleLogin(Scanner scanner, ServerFacade facade) throws ResponseException {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        AuthData result = facade.login(username, password);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged in as " + result.username() + EscapeSequences.RESET_TEXT_COLOR);
        return result;
    }
}
