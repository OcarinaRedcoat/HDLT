package pt.tecnico.sec.hdlt;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class IOUtils {

    public static User readUser(String gridFileLocation){
        Scanner scanner = new Scanner(System.in);
        User user;
        System.out.println("Specify the client ID: ");
        do {
            try{
                int user_id = Integer.parseInt(scanner.nextLine());
                user = FileUtils.parseGridUser(gridFileLocation, user_id);

                return user;
            } catch (ParseException | IOException | IndexOutOfBoundsException | NumberFormatException | NoSuchElementException | IllegalStateException e) {
                System.out.println("Invalid Client ID. Try again: ");
            }
        } while(true);
    }

    public static String readGridFileLocation(){
        return readString("Specify the grid file location: ");
    }

    private static String readString(String askMessage){
        Scanner scanner = new Scanner(System.in);
        if(!askMessage.equals("")){
            System.out.println(askMessage);
        }
        return scanner.nextLine();
    }
}
