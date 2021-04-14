package pt.tecnico.sec.hdlt.client;

import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.LocationReport;

import java.util.Scanner;

import static pt.tecnico.sec.hdlt.GeneralUtils.getCurrentEpoch;
import static pt.tecnico.sec.hdlt.IOUtils.*;

public class Main
{
    //TODO: G:\IST\2-Semestre\SEC\Projeto\HDLT\grids.output.json
    public static void main(String[] args) {
        Client.getInstance().initializeUser(readUser());

        int f = readInteger("Define f: ");

        UserServer.getInstance().start();

        String command;
        long epoch;
        LocationReport report;

        System.out.println("Client Initialized. Type \"help\" at any point for the list of available commands.");
        do{
            command = readString(null);
            switch (command){
                case "help":
                    System.out.println("Available commands: \"help\", \"submit report\"(it will also request witnesses automatically), \"current epoch\", \"obtain report\", \"exit\".");
                    break;
                case "submit report":
                    epoch = readEpoch();
                    report = UserClient.getInstance().requestLocationProofs(epoch, f);
                    if(report != null)
                        UserClient.getInstance().submitLocationReport(report);
                    break;
                case "obtain report":
                    epoch = readEpoch();
                    UserClient.getInstance().obtainLocationReport(epoch);
                    break;
                case "current epoch":
                    System.out.println("Current Epoch: " + getCurrentEpoch());
                    break;
                case "redefine f":
                    f = readInteger("New f value: ");
                    break;
                case "current f":
                    System.out.println("Current f: " + f);
                    break;
                case "exit":
                    break;
                default:
                    System.out.println("Invalid Command. Type \"help\" for available commands.");
            }
        }while(!command.equals("exit"));

        UserServer.getInstance().stop();
    }

}
