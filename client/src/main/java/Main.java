import client.ServerFacade;
import ui.PostLoginLoop;
import ui.PreLoginLoop;

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

        while (true) {
            try {
                String authToken = new PreLoginLoop().run(scanner, facade);
                if ("QUIT".equals(authToken)) {
                    break;
                }
                new PostLoginLoop().run(scanner, facade, authToken);
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                System.out.println("Please try again or contact support if the problem persists.");
            }
        }
        
        scanner.close();
    }
}
