package pt.tecnico.sec.hdlt.haclient;

import pt.tecnico.sec.hdlt.communication.SignedLocationReport;
import pt.tecnico.sec.hdlt.haclient.communication.HAClient;
import pt.tecnico.sec.hdlt.haclient.ha.HA;

import java.util.List;
import java.util.Scanner;

import static pt.tecnico.sec.hdlt.IOUtils.*;

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

        HAClient.getInstance();

        System.out.println("||| HA INITIALIZED |||");
        do {
            printCommands();
            command = readInteger(null);
            switch (command) {
                case 1:
                    epoch = readEpoch();
                    userId = readUserId();
                    SignedLocationReport report = HAClient.getInstance().obtainLocationReport(userId, epoch);
                    break;
                case 2:
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
                case 3:
                    break;
                default:
                    System.out.println("Invalid Command.");
            }
        } while (command != 3);

        HAClient.getInstance().serverShutdown();
    }
}
