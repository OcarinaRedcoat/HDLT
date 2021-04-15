package pt.tecnico.sec.hdlt.server;

import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class App {

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Usage: LocationServer <# of byzantine users>");
            return;
        }

        int f = Integer.parseInt(args[0]);
        if (f < 0) {
            System.err.println("The number of byzantine users has to be zero or greater");
            return;
        }

        try {
            LocationBL locationBL = new LocationBL(1, f);

            LocationServer locationServer = new LocationServer();
            locationServer.start(locationBL);

            System.out.println("Type <q> or <quit> or <exit> to shutdown the server.");

            Scanner scanner = new Scanner(System.in);
            String input;
            do {
                System.out.print("> ");
                input = scanner.nextLine().trim();
            } while (!input.equals("q") && !input.equals("quit") && !input.equals("exit"));

            System.out.println("SHUTTING SERVER DOWN!");
            locationServer.stop();
            locationBL.terminateWriteQueue();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}