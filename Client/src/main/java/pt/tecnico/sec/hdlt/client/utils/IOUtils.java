package pt.tecnico.sec.hdlt.client.utils;

import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.client.user.User;
import pt.tecnico.sec.hdlt.client.user.UserClient;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

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
                scanner.close();
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

    private static String readString(String askMessage){
        Scanner scanner = new Scanner(System.in);
        if(!askMessage.equals("")){
            System.out.println(askMessage);
        }
        return scanner.nextLine();
    }
}
