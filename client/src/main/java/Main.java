import java.util.Scanner;
import client.ServerFacade;
import ui.PreLoginLoop;
import ui.PostLoginLoop;

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

        String authToken = new PreLoginLoop().run(scanner, facade);

        // Run Post-login loop: handles listing, creating, joining games
        new PostLoginLoop().run(scanner, facade, authToken);

        scanner.close();
    }
}
