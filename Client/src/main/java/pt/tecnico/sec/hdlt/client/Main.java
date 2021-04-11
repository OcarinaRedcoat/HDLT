package pt.tecnico.sec.hdlt.client;

import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.LocationProofBetweenClientsResponse;

import java.util.ArrayList;
import java.util.Scanner;

import static pt.tecnico.sec.hdlt.GeneralUtils.getCurrentEpoch;
import static pt.tecnico.sec.hdlt.IOUtils.*;

/**
 * Hello world!
 *
 */
public class Main
{
    //TODO: G:\IST\2-Semestre\SEC\Projeto\HDLT\grids.output.json
    public static void main(String[] args) {
        Client.getInstance().setUser(readUser(readGridFileLocation()));
        UserServer.getInstance().start();

        Scanner scanner = new Scanner(System.in);
        String command;
        System.out.println("Client Initialized. Type \"help\" at any point for the list of available commands.");
        do{
            command = scanner.nextLine();
            switch (command){
                case "exit":
                    break;
                case "submit report":
                    ArrayList<LocationProofBetweenClientsResponse> proofs = UserClient.getInstance().requestLocationProofs(getCurrentEpoch());
                    //UserClient.getInstance().submitLocationReport();
                    break;
                case "obtain report":
                    UserClient.getInstance().obtainLocationReport(getCurrentEpoch());
                    break;
                case "current epoch":
                    System.out.println("Current Epoch: " + getCurrentEpoch());
                    break;
                case "help":
                    System.out.println("Available commands: \"help\", \"submit report\", \"current epoch\", \"obtain report\", \"exit\".");
                    break;
                default:
                    System.out.println("Invalid Command. Type \"help\" for available commands.");
            }
        }while(!command.equals("exit"));

        UserServer.getInstance().stop();
    }

}
