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

    public static int readF(){
        do {
            try{
                return readInteger("Define f: ");
            } catch (NumberFormatException | NoSuchElementException | IllegalStateException e) {
                System.out.println("Invalid f. Try again: ");
            }
        } while(true);
    }

    private static String readGridFileLocation(){
        return readString("Specify the grid file location: ");
    }

    public static int readUserId(){
        do {
            try{
                return readInteger("Specify the client ID: ");
            } catch (IndexOutOfBoundsException | NumberFormatException | NoSuchElementException |
                    IllegalStateException e) {

                System.out.println("Invalid Client ID. Try again: ");
            }
        } while(true);

    }

    public static long readEpoch(){
        do {
            try{
                return readLong("Specify the epoch (we are asking for the current epoch for the purpose of testing): ");
            } catch (NumberFormatException | NoSuchElementException | IllegalStateException e) {
                System.out.println("Invalid epoch. Try again: ");
            }
        } while(true);
    }

    public static String readString(String askMessage){
        Scanner scanner = new Scanner(System.in);
        if(askMessage != null){
            System.out.println(askMessage);
        }
        return scanner.nextLine();
    }

    private static int readInteger(String askMessage) throws NumberFormatException{
        return Integer.parseInt(readString(askMessage));
    }

    private static long readLong(String askMessage) throws NumberFormatException{
        return Long.parseLong(readString(askMessage));
    }
}
