package pt.tecnico.sec.hdlt.client;

import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.client.user.Client;
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
import static pt.tecnico.sec.hdlt.client.utils.IOUtils.readUser;
import static pt.tecnico.sec.hdlt.client.utils.IOUtils.setGridFile;

/**
 * Hello world!
 *
 */
public class Main
{

    //TODO: G:\IST\2-Semestre\SEC\Projeto\HDLT\grids.output.json
    public static void main(String[] args) {

        setGridFile();

        readUser();

        //########################################################
        //#################### TODO start server ######################
        //########################################################
        try {
            UserServer.getInstance().start();
        } catch (IOException e) {
            System.out.println("Could not start server.");
            System.exit(1);
        }

        //########################################################
        //################### TODO: não está a dar Request Inputs #####################
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
                    UserClient.getInstance().requestLocationProof(getCurrentEpoch());
                    break;
                case "current epoch":
                    System.out.println("Current Epoch: " + getCurrentEpoch());
                    break;
                case "help":
                    System.out.println("Available commands: help, request proofs, exit.");
                    break;
                default:
                    System.out.println("Invalid Command. Type \"help\" for available commands.");
            }
        }while(!command.equals("exit"));


        //########################################################
        //################## server shutdown #####################
        //########################################################
        try {
            UserServer.getInstance().blockUntilShutdown();
            UserServer.getInstance().stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
