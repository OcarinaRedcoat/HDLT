package pt.tecnico.sec.hdlt.client;

import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.client.user.User;
import pt.tecnico.sec.hdlt.client.user.UserClient;
import pt.tecnico.sec.hdlt.client.user.UserServer;
import pt.tecnico.sec.hdlt.client.utils.FileUtils;
import pt.tecnico.sec.hdlt.client.utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.TimeZone;

import static pt.tecnico.sec.hdlt.client.utils.GeneralUtils.getCurrentEpoch;
import static pt.tecnico.sec.hdlt.client.utils.IOUtils.readString;
import static pt.tecnico.sec.hdlt.client.utils.IOUtils.readUser;

/**
 * Hello world!
 *
 */
public class Main
{

    public static void main(String[] args) {

        //TODO: Remover isto antes de entregar:  G:\IST\2-Semestre\SEC\Projeto\HDLT\grids.output.json
        String fridFileLocation = readString("Specify the grid file location: ");

        User user = readUser(fridFileLocation);


        //########################################################
        //#################### TODO start server ######################
        //########################################################
        UserServer server = null;
        try {
            server = new UserServer(user);
        } catch (IOException e) {
            System.out.println("Could not start server.");
            System.exit(1);
        }
        UserClient client = UserClient.getInstance();



        //########################################################
        //################### TODO Request Inputs #####################
        //########################################################
        Scanner scanner = new Scanner(System.in);
        String command;
        System.out.println("Client Initialized. Type \"Help\" at any point for the list of available commands.");
        do{
            command = scanner.nextLine();
            switch (command){
                case "exit":
                    break;
                case "request proofs":

                    client.requestLocationProof(user, getCurrentEpoch());
                    break;
                case "current epoch":

                    break;
                case "help":
                    System.out.println("Available commands: help, request proofs, exit.");
                    break;
                default:
                    System.out.println("Invalid Command. Type \"Help\" for available commands.");
            }
        }while(!command.equals("exit"));


        //########################################################
        //################## server shutdown #####################
        //########################################################
        try {
            server.blockUntilShutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //########################################################
        //################## TODO client shutdown #####################
        //########################################################

    }

}
