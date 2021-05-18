package pt.tecnico.sec.hdlt.client;

import pt.tecnico.sec.hdlt.client.services.UserClient;
import pt.tecnico.sec.hdlt.client.services.Server;
import pt.tecnico.sec.hdlt.entities.Client;
import pt.tecnico.sec.hdlt.communication.LocationReport;

import static pt.tecnico.sec.hdlt.utils.GeneralUtils.*;
import static pt.tecnico.sec.hdlt.utils.IOUtils.*;

public class Main
{
    private static void printCommands(){
        System.out.println("-----------------------------");
        System.out.println("Available commands:");
        System.out.println("(1) Submit a report");
        System.out.println("(2) Obtain a report");
        System.out.println("(3) Obtain my proofs");
        System.out.println("(4) Current epoch");
        System.out.println("(5) Defined F");
        System.out.println("(6) Number of servers");
        System.out.println("(7) Exit");
        System.out.println("-----------------------------");
    }

    //     ../grids.output.json
    public static void main(String[] args) {
        Client client = new Client(readUser());
        Server serverGrpc = new Server(client);
        UserClient clientGrpc = new UserClient(client);

        int command;
        System.out.println("||| CLIENT INITIALIZED |||");
        do{
            printCommands();
            command = readCommand();
            switch (command){
                case 1:
                    LocationReport.Builder reportBuilder = clientGrpc.requestLocationProofs(readEpoch(), F);
                    if(reportBuilder != null)
                        clientGrpc.submitLocationReport(reportBuilder);
                    break;
                case 2:
                    clientGrpc.obtainLocationReport(readEpoch());
                    break;
                case 3:
                    clientGrpc.obtainMyProofs(readEpochs());
                    break;
                case 4:
                    System.out.println("Current Epoch: " + getCurrentEpoch());
                    break;
                case 5:
                    System.out.println("Defined F = " + F);
                    break;
                case 6:
                    System.out.println("Number of servers = " + N_SERVERS);
                    break;
                case 7:
                    break;
                default:
                    System.out.println("Invalid Command.");
            }
        }while(command != 7);

        clientGrpc.closeServerChannel();
        serverGrpc.stop();
    }

}
