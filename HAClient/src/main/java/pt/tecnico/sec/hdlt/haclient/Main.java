package pt.tecnico.sec.hdlt.haclient;

import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.communication.SignedLocationReport;
import pt.tecnico.sec.hdlt.haclient.communication.HAClient;
import pt.tecnico.sec.hdlt.haclient.ha.HA;

import java.util.List;

import static pt.tecnico.sec.hdlt.utils.IOUtils.*;

public class Main {

    private static void printCommands(){
        System.out.println("-----------------------------");
        System.out.println("Available commands:");
        System.out.println("(1) Obtain a location report");
        System.out.println("(2) Obtain users at location");
        System.out.println("(3) Exit");
        System.out.println("-----------------------------");
    }

    public static void main(String[] args) {

        long epoch;
        int userId, command;

        HA myHA = new HA();
        HAClient haClient = new HAClient(myHA);

        System.out.println("||| HA INITIALIZED |||");
        do {
            printCommands();
            command = readCommand();
            switch (command) {
                case 1:
                    epoch = readEpoch();
                    userId = readUserId();
                    haClient.obtainLocationReport(userId, epoch);
                    break;
                case 2:
                    epoch = readEpoch();
                    long x = readPosX();
                    long y = readPosY();
                    haClient.obtainUsersAtLocation(x, y, epoch);
                    break;
                case 3:
                    break;
                default:
                    System.out.println("Invalid Command.");
            }
        } while (command != 3);

        haClient.serverShutdown();
    }
}
