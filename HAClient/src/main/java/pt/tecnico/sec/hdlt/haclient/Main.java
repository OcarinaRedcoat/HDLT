package pt.tecnico.sec.hdlt.haclient;

import pt.tecnico.sec.hdlt.communication.SignedLocationReport;
import pt.tecnico.sec.hdlt.haclient.communication.HAClient;
import pt.tecnico.sec.hdlt.haclient.ha.HA;

import java.util.List;
import java.util.Scanner;

import static pt.tecnico.sec.hdlt.IOUtils.*;

public class Main {

    public static void main(String[] args) {

        String command;
        long epoch;
        int userId;

        HAClient.getInstance();

        System.out.println("HA Started. Type \"help\" at any point for the list of available commands.");
        do {
            command = readString(null);
            switch (command) {
                case "exit":
                    break;
                case "obtain location report":
                    epoch = readEpoch();
                    userId = readUserId();
                    SignedLocationReport report = HAClient.getInstance().obtainLocationReport(userId, epoch);
                    break;
                case "obtain user at location":
                    epoch = readEpoch();
                    long x = readPos("Specify the posX: ");
                    long y = readPos("Specify the posY: ");
                    List<SignedLocationReport> listReport = HAClient.getInstance().obtainUsersAtLocation(x, y, epoch);
                    if (listReport != null) {
                        for (SignedLocationReport rep: listReport) {
                            System.out.println(rep);
                        }
                    }
                    break;
                case "help":
                    System.out.println("Available commands: \"help\", \"obtain location report\", \"obtain user at location\", \"exit\".");
                    break;
                default:
                    System.out.println("Invalid Command. Type \"help\" for available commands.");
            }
        } while (!command.equals("exit"));
        HAClient.getInstance().serverShutdown();
    }
}
