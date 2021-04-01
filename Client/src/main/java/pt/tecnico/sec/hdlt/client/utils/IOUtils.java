package pt.tecnico.sec.hdlt.client.utils;

import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.client.user.User;

import java.io.IOException;
import java.util.Scanner;

public class IOUtils {

    public static User readUser(String gridFileLocation){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Specify the client ID: ");
        do {
            try{
                int user_id = Integer.parseInt(scanner.nextLine());
                scanner.close();
                return FileUtils.parseGridUser(gridFileLocation, user_id);
            } catch (ParseException | IOException | IndexOutOfBoundsException e) {
                System.out.println("Invalid Client ID. Try again: ");
            }
        } while(true);
    }

    public static String readString(String askMessage){
        Scanner scanner = new Scanner(System.in);
        System.out.println(askMessage);
        return scanner.nextLine();
    }
}
