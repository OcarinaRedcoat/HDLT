package pt.tecnico.sec.hdlt;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import static pt.tecnico.sec.hdlt.GeneralUtils.getCurrentEpoch;
import static pt.tecnico.sec.hdlt.IOUtils.*;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {

        int haId = Integer.parseInt(args[1]);

        Scanner scanner = new Scanner(System.in);
        String command;
        long epoch;

        System.out.println("HA Started. Type \"help\" at any point for the list of available commands.");
        do {
            command = scanner.nextLine();
            switch (command) {
                case "exit":
                    break;
                case "obtain location report": // arg
                    epoch = Integer.parseInt(readString("Specify the epoch (we are not using the current epoch for the purpose of testing): "));
                    int userId = Integer.parseInt(readString("Specify the id of the user that you want the location report: "));
                    //report = UserClient.getInstance().requestLocationProofs(epoch);
                    //UserClient.getInstance().submitLocationReport(report);
                    //HAClient.getInstance().obtainLocationReport();
                    break;
                case "obtain user at location":
                    //epoch = Integer.parseInt(readString("Specify the epoch: "));
                    //UserClient.getInstance().obtainLocationReport(epoch);
                    HAClient.getInstance().obtainUsersAtLocation();
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
