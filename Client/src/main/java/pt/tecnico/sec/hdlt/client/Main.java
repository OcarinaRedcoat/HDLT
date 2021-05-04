package pt.tecnico.sec.hdlt.client;

import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
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
        UserServer serverGrpc = new UserServer(client);
        UserClient clientGrpc = new UserClient();

        int command;

        System.out.println("||| CLIENT INITIALIZED |||");
        do{
            printCommands();
            command = readCommand();
            switch (command){
                case 1:
                    LocationReport.Builder reportBuilder = clientGrpc.requestLocationProofs(client, readEpoch(), F);
                    if(reportBuilder != null)
                        clientGrpc.submitLocationReport(client, reportBuilder);
                    break;
                case 2:
                    clientGrpc.obtainLocationReport(client, readEpoch());
                    break;
                case 3:
                    clientGrpc.obtainMyProofs(client, readEpochs());
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
