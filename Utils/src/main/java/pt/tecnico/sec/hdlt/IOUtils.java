package pt.tecnico.sec.hdlt;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class IOUtils {

    public static String readLine(){
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    public static int readInt(String errMessage){
        do{
            Scanner scanner = new Scanner(System.in);
            if(scanner.hasNextInt())
                return scanner.nextInt();
            System.out.println(errMessage);
        } while(true);
    }

    public static Long readLong(String errMessage){
        do{
            Scanner scanner = new Scanner(System.in);
            if(scanner.hasNextLong())
                return scanner.nextLong();
            System.out.println(errMessage);
        } while(true);
    }

    public static List<Long> readLongs(String errorMessage){
        Scanner scanner = new Scanner(System.in);
        List<Long> list = new ArrayList<>();
        do{
            while (scanner.hasNextLong())
                list.add(scanner.nextLong());

            if(list.size() == 0)
                System.err.println(errorMessage);
        } while(list.size() != 0);

        return list;
    }

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

    private static String readGridFileLocation(){
        System.out.println("Specify the grid file location: ");
        return readLine();
    }

    public static int readUserId(){
        System.out.println("Specify the client ID: ");
        return readInt("Invalid Client ID. Try again: ");
    }

    public static long readEpoch(){
        System.out.println("Specify the epoch (we are asking for the current epoch since we didn't generate grid location for current epoch): ");
        return readLong("Invalid epoch. Try again: ");
    }

    public static List<Long> readEpochs(){
        System.out.println("Specify epochs: (Example: 1 5 12 4 42)");
        return readLongs("Invalid epochs. Try again: ");
    }

    public static long readPosX(){
        System.out.println("Specify the posX: ");
        return readLong("Invalid position. Try again: ");
    }

    public static long readPosY(){
        System.out.println("Specify the posY: ");
        return readLong("Invalid position. Try again: ");
    }

    public static int readCommand(){
        return readInt("Input must be a number. Try again: ");
    }

    public static String readPassword(String message){
        System.out.println(message);
        return readLine();
    }

}
