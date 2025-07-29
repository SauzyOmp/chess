import client.ServerFacade;
import ui.PostLoginLoop;
import ui.PreLoginLoop;
import service.LoginResult;
import ui.EscapeSequences;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        ServerFacade facade = new ServerFacade(port);
        Scanner scanner = new Scanner(System.in);

        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.SET_TEXT_BOLD + "♕ Welcome to Chess!" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "==================" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + EscapeSequences.SET_TEXT_BOLD + "Available commands:" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + "help" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (h)" + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Show detailed help for current state");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + "register" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (r)" + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Create a new account");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + "login" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (l)" + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Sign in to your account");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_RED + "quit" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (q)" + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Exit the program");
        System.out.println();
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "==================" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();

        while (true) {
            try {
                LoginResult loginResult = new PreLoginLoop().run(scanner, facade);
                new PostLoginLoop().run(scanner, facade, loginResult.authToken(), loginResult.username());
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                System.out.println("Please try again or contact support if the problem persists.");
            }
        }
    }
}
