package pt.tecnico.sec.hdlt.server;

import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class App {

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length != 1) {
            System.err.println("Usage: LocationServer <# of byzantine users>");
            return;
        }

        try {
            LocationBL locationBL = new LocationBL(1, Integer.parseInt(args[0]));

            LocationServer locationServer = new LocationServer();
            locationServer.start(locationBL);
            locationServer.blockUntilShutdown();

            locationBL.terminateWriteQueue();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
}