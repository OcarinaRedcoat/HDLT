package pt.tecnico.sec.hdlt;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class IOUtils {

    public static User readUser(){
        do {
            try{
                return FileUtils.parseGridUser(readGridFileLocation(), readUserId());
            } catch (ParseException | IOException | IndexOutOfBoundsException | NumberFormatException |
                    NoSuchElementException | IllegalStateException e) {
                System.out.println("Invalid Client ID or Grid file type or location. Try again: ");
            }
        } while(true);
    }

    public static String readGridFileLocation(){
        return readString("Specify the grid file location: ");
    }

    public static int readUserId(){
        return readInteger("Specify the client ID: ");
    }

    public static long readEpoch(){
        return readLong("Specify the epoch (we are asking for the current epoch for the purpose of testing): ");
    }

    public static String readString(String askMessage){
        Scanner scanner = new Scanner(System.in);
        if(askMessage != null){
            System.out.println(askMessage);
        }
        return scanner.nextLine();
    }

    public static int readInteger(String askMessage){
        return Integer.parseInt(readString(askMessage));
    }

    public static long readLong(String askMessage){
        return Long.parseLong(readString(askMessage));
    }
}
