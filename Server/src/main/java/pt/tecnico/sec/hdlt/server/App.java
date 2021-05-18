package pt.tecnico.sec.hdlt.server;

import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import java.util.Scanner;

import static pt.tecnico.sec.hdlt.utils.IOUtils.readLine;

public class App {

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: LocationServer <Server Id> <KeyStore password>"); // for server 1 pwd = server_1
            return;
        }

        int id = Integer.parseInt(args[0]);
        LocationServer locationServer = new LocationServer(id, new LocationBL(id, args[1]));

        System.out.println("||| SERVER INITIALIZED |||");
        System.out.println("Type <q> or <quit> or <exit> to shutdown the server.");

        String input;
        do {
            input = readLine();
        } while (!input.equals("q") && !input.equals("quit") && !input.equals("exit"));

        locationServer.stop();
    }
}