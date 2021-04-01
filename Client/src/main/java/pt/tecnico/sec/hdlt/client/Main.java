package pt.tecnico.sec.hdlt.client;

import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.client.user.User;
import pt.tecnico.sec.hdlt.client.user.UserClient;
import pt.tecnico.sec.hdlt.client.user.UserServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.TimeZone;

/**
 * Hello world!
 *
 */
public class Main
{

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);


        //########################################################
        //###################### get grid ########################
        //########################################################
        System.out.println("Specify the grid file location: ");
        String read_line;
        Grid grid = new Grid();
        boolean operationNotSuccessful = true;
        do {
            try{
                //TODO: Remover isto antes de entregar:  G:\IST\2-Semestre\SEC\Projeto\HDLT\grids.output.json
                read_line = scanner.nextLine();
                grid.parseFiles(read_line);
                operationNotSuccessful = false;
            } catch (ParseException | IOException e) {
                System.out.println("Could not open file. Try again: ");
            }
        } while(operationNotSuccessful);


        //########################################################
        //##################### get epoch ########################
        //########################################################
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long currentTimeStep = Math.floorDiv(calendar.getTimeInMillis() / 1000, 604800); //TODO: Timesteps of 1 week, changed it to a global variable
        System.out.println("Current Epoch: " + currentTimeStep);


        //########################################################
        //##################### get client #######################
        //########################################################
        System.out.println("Specify the client ID: ");
        int client_id;
        User myUser = null;
        operationNotSuccessful = true;
        do {
            try{
                read_line = scanner.nextLine();

                client_id = Integer.parseInt(read_line);
                myUser = grid.getMyUser(client_id, currentTimeStep);

                operationNotSuccessful = false;
            } catch (Exception e) {
                System.out.println("Invalid Client ID.");
            }
        } while(operationNotSuccessful);


        //########################################################
        //#################### TODO start server ######################
        //########################################################
        UserServer server = new UserServer(myUser);

        try {
            server.start();
            server.blockUntilShutdown(); //??????? No git tem não sei se aqui faz sentido
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<User> users_that_are_close = grid.usersClosedBy(myUser.getCloseBy(),0);

        UserClient client = new UserClient(myUser, users_that_are_close);

        client.requestLocationProof(myUser, 0);


        //########################################################
        //################### TODO Request Inputs #####################
        //########################################################
        System.out.println("Client Initialized. Type \"Help\" at any point for the list of available commands.");
        String command;
        do{
            command = scanner.nextLine();
            switch (command){
                case "exit":
                    break;
                default:
                    System.out.println("Invalid Command. Type \"Help\" for available commands.");
            }
        }while(!command.equals("exit"));


        //########################################################
        //###################### TODO shutdown ########################
        //########################################################
        // TODO Fazer o shutdown dos channels ......
    }

}
