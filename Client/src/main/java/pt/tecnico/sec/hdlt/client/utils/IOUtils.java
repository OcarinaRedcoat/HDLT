package pt.tecnico.sec.hdlt.client.utils;

import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.client.user.User;
import pt.tecnico.sec.hdlt.client.communication.UserClient;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static pt.tecnico.sec.hdlt.client.utils.GeneralUtils.getCurrentEpoch;

public class IOUtils {

    public static void readUser(){
        Scanner scanner = new Scanner(System.in);
        User user;
        System.out.println("Specify the client ID: ");
        do {
            try{
                int user_id = Integer.parseInt(scanner.nextLine());
                user = FileUtils.getInstance().parseGridUser(user_id);
                Client.getInstance().setUser(user);
                return;
            } catch (ParseException | IOException | IndexOutOfBoundsException | NumberFormatException | NoSuchElementException | IllegalStateException e) {
                System.out.println("Invalid Client ID. Try again: ");
            }
        } while(true);
    }

    public static void setGridFile(){
        do {
            try {
                String gridFileLocation = readString("Specify the grid file location: ");
                FileUtils.getInstance().setGridFileLocation(gridFileLocation);
                return;
            } catch (IOException e) {
                System.out.println("Invalid file location. Try again. ");
            }
        } while(true);
    }

    //TODO: not working
    public static void startUserInteraction(){
        Scanner scanner = new Scanner(System.in);
        String command;
        System.out.println("Client Initialized. Type \"help\" at any point for the list of available commands.");
        do{
            command = scanner.nextLine();
            switch (command){
                case "exit":
                    break;
                case "submit report":
                    UserClient.getInstance().requestLocationProof(getCurrentEpoch());
                    //UserClient.getInstance().submitLocationReport();
                    break;
                case "obtain report":
                    //UserClient.getInstance().obtainLocationReport();
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
    }

    private static String readString(String askMessage){
        Scanner scanner = new Scanner(System.in);
        if(!askMessage.equals("")){
            System.out.println(askMessage);
        }
        return scanner.nextLine();
    }
}
