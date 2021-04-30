package pt.tecnico.sec.hdlt.server;

import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import java.util.Scanner;

public class App {

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: LocationServer <Server Id> <KeyStore password>"); // for server 1 pwd = server_1
            return;
        }

        try {
            LocationBL locationBL = new LocationBL(Integer.parseInt(args[0]), args[1]);

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
            locationBL.terminateMessageWriteQueue();
            locationBL.terminateNonceWriteQueue();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}