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
                System.err.println("Invalid Client ID or Grid file type or location. Try again: ");
            }
        } while(true);
    }

    public static User readUser(String gridLocation, int userId){
        do {
            try{
                return FileUtils.parseGridUser(gridLocation, userId);
            } catch (ParseException | IOException | IndexOutOfBoundsException | NumberFormatException |
                    NoSuchElementException | IllegalStateException e) {
                System.err.println("Invalid Client ID or Grid file type or location. Try again: ");
            }
        } while(true);
    }

    public static int readF(){
        do {
            try{
                int f = readInteger("Define f: ");
                if(f<0){
                    System.err.println("f has to be >= 0!");
                    continue;
                }
                return f;
            } catch (NumberFormatException | NoSuchElementException | IllegalStateException e) {
                System.err.println("Invalid f. Try again: ");
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

                System.err.println("Invalid Client ID. Try again: ");
            }
        } while(true);

    }

    public static long readEpoch(){
        do {
            try{
                return readLong("Specify the epoch (we are asking for the current epoch for the purpose of testing): ");
            } catch (NumberFormatException | NoSuchElementException | IllegalStateException e) {
                System.err.println("Invalid epoch. Try again: ");
            }
        } while(true);
    }

    public static long readPos(String message){
        do {
            try{
                return readLong(message);
            } catch (NumberFormatException | NoSuchElementException | IllegalStateException e) {
                System.err.println("Invalid position. Try again: ");
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

    public static int readInteger(String askMessage){
        return Integer.parseInt(readString(askMessage));
    }

    public static long readLong(String askMessage){
        return Long.parseLong(readString(askMessage));
    }
}
